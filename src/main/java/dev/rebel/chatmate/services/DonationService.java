package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream.LivestreamStatus;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.models.DonationEventData;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import dev.rebel.chatmate.stores.DonationApiStore;
import dev.rebel.chatmate.stores.LivestreamApiStore;
import dev.rebel.chatmate.stores.RankApiStore;
import dev.rebel.chatmate.util.Memoiser;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DonationService {
  private final DateTimeService dateTimeService;
  private final DonationApiStore donationApiStore;
  private final LivestreamApiStore livestreamApiStore;
  private final RankApiStore rankApiStore;
  private final Memoiser memoiser;

  public DonationService(DateTimeService dateTimeService, DonationApiStore donationApiStore, LivestreamApiStore livestreamApiStore, RankApiStore rankApiStore, ChatMateEventService chatMateEventService) {
    this.dateTimeService = dateTimeService;
    this.donationApiStore = donationApiStore;
    this.livestreamApiStore = livestreamApiStore;
    this.rankApiStore = rankApiStore;
    this.memoiser = new Memoiser();

    chatMateEventService.onDonation(this::onNewDonation, null);
  }

  // donating adds a visual effect to the user's name in chat, whose duration depends on the donation amount and user's rank.
  // the duration is used up only during livestreams, and are additive, so calculating whether an effect should be shown right
  // now is non-trivial.
  public boolean shouldShowDonationEffect(int userId) {
    List<PublicDonation> allDonations = this.donationApiStore.getDonations();
    List<PublicLivestream> allLivestreams = this.livestreamApiStore.getLivestreams();

    // memoised so we don't do this every frame for every user in chat
    long showEffectUntil = this.memoiser.memoise(String.valueOf(userId), () -> {
      List<PublicDonation> donations = Collections.filter(allDonations, d -> d.linkedUser != null && Objects.equals(d.linkedUser.id, userId));
      if (donations.size() == 0) {
        return -1L;
      }

      boolean isDonator = EnumHelpers.getFirst(Collections.map(this.rankApiStore.getCurrentUserRanks(userId), r -> r.rank.name), RankName.DONATOR, RankName.SUPPORTER, RankName.MEMBER) != null;
      if (!isDonator) {
        return -1L;
      }

      List<PublicLivestream> livestreams = Collections.filter(allLivestreams, l -> l.status != LivestreamStatus.NotStarted);
      long now = this.dateTimeService.now();

      // we go forward over each donation, then over each livestream, and figure out if the user has any effect-time credited at the current instance in time.
      // to do this, we transform all livestream run times into a continuous timeline, and match the donation times to that timeline
      List<Tuple2<Long, Long>> mappedDonations = new ArrayList<>(); // (time, duration) pairs
      for (PublicDonation donation : donations) {
        long t = 0;
        for (PublicLivestream livestream : livestreams) {
          long endTime = livestream.endTime == null ? now : livestream.endTime;
          if (donation.time < livestream.startTime) {
            // donation was made before this livestream started
            // equivalent to occurring at the exact start of the livestream
            break;

          } else if (donation.time >= livestream.startTime && donation.time <= endTime) {
            // donation was made during livestream
            t += donation.time - livestream.startTime;
            break;

          } else {
            // donation was made after this livestream ended - keep going
            t += endTime - livestream.startTime;
          }
        }

        // important: we are getting the ranks at the time the donation was linked (not at the time it was posted), because only
        // at that time would the server have updated the user's ranks due to the donation
        List<RankName> ranks = Collections.map(this.rankApiStore.getUserRanksAtTime(userId, donation.linkedAt), r -> r.rank.name);
        int minutesPerDollar;
        if (ranks.contains(RankName.MEMBER)) {
          minutesPerDollar = 15;
        } else if (ranks.contains(RankName.SUPPORTER)) {
          minutesPerDollar = 10;
        } else if (ranks.contains(RankName.DONATOR)) {
          minutesPerDollar = 5;
        } else {
          // this happens if the ranks haven't loaded yet
          minutesPerDollar = 0;
        }
        long effectDuration = (long)(donation.amount * minutesPerDollar * 60 * 1000);

        mappedDonations.add(new Tuple2<>(t, effectDuration));
      }

      long totalLivestreamDuration = 0;
      for (PublicLivestream livestream : livestreams) {
        long endTime = livestream.endTime == null ? now : livestream.endTime;
        totalLivestreamDuration += endTime - livestream.startTime;
      }

      // finally, process the (possibly overlapping) effect durations due to every donation
      long currentEffectUntil = 0;
      for (Tuple2<Long, Long> pair : mappedDonations) {
        long t = pair._1;
        long duration = pair._2;

        if (currentEffectUntil > t) {
          currentEffectUntil += duration;
        } else {
          currentEffectUntil = t + duration;
        }
      }

      long leftOverEffect = currentEffectUntil - totalLivestreamDuration;
      if (leftOverEffect > 0) {
        return now + leftOverEffect;
      } else {
        return -1L;
      }
    }, allDonations, allLivestreams, this.rankApiStore.getStateToken(userId));

    return this.dateTimeService.now() < showEffectUntil;
  }

  private DonationEventData.Out onNewDonation(DonationEventData.In in) {
    this.donationApiStore.clear();
    return new DonationEventData.Out();
  }
}
