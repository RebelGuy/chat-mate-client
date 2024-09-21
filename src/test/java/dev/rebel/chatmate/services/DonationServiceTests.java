package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicAggregateLivestream;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.stores.DonationApiStore;
import dev.rebel.chatmate.stores.LivestreamApiStore;
import dev.rebel.chatmate.stores.RankApiStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DonationServiceTests {
  private PublicAggregateLivestream livestream1; // from hour 0 to hour 1
  private PublicAggregateLivestream livestream2; // from hour 2 to hour 3
  private PublicAggregateLivestream livestream3; // from hour 4 onwards
  private long now; // hour 5
  private int userId;

  @Mock DateTimeService mockDateTimeService;
  @Mock DonationApiStore mockDonationApiStore;
  @Mock LivestreamApiStore mockLivestreamApiStore;
  @Mock RankApiStore mockRankApiStore;
  @Mock ChatMateEventService mockChatMateEventService;
  DonationService donationService;

  @Before
  public void Setup() {
    this.donationService = new DonationService(this.mockDateTimeService, this.mockDonationApiStore, this.mockLivestreamApiStore, this.mockRankApiStore, this.mockChatMateEventService);

    long hour = 3600 * 1000L;
    this.livestream1 = new PublicAggregateLivestream() {{
      startTime = 0L;
      endTime = hour;
    }};
    this.livestream2 = new PublicAggregateLivestream() {{
      startTime = 2 * hour;
      endTime = 3 * hour;
    }};
    this.livestream3 = new PublicAggregateLivestream() {{
      startTime = 4 * hour;
      endTime = null;
    }};

    this.userId = 1;
    this.now = 5 * hour;
    when(this.mockDateTimeService.now()).thenReturn(this.now);

    when(this.mockLivestreamApiStore.getData()).thenReturn(Collections.list(this.livestream1, this.livestream2, this.livestream3));
    when(this.mockRankApiStore.getUserRanksAtTime(eq(1), anyLong())).thenReturn(createRanks(RankName.DONATOR));
    when(this.mockRankApiStore.getCurrentUserRanks(1)).thenReturn(createRanks(RankName.DONATOR));
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfNoDonations() {
    when(mockDonationApiStore.getData()).thenReturn(new ArrayList<>());

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfSingleDonationTooLongAgo() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
       createDonation(10f, this.livestream3.startTime) // effect should run up to 10 minutes before now
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfDoubleDonationTooLongAgo() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
       createDonation(5f, this.livestream3.startTime),
       createDonation(5f, this.livestream3.startTime) // effect should run up to 10 minutes before now
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsTrue_IfSingleDonationRecent() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(15f, this.livestream3.startTime)
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsTrue_IfDoubleDonationRecent() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(10f, this.livestream3.startTime),
        createDonation(10f, this.livestream3.startTime)
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfSingleDonationRecentButNotDonatorRank() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(15f, this.livestream3.startTime)
    ));
    when(mockRankApiStore.getCurrentUserRanks(1)).thenReturn(createRanks(RankName.FAMOUS));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsTrue_IfBigDonationBetweenLivestreams() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(100f, this.livestream1.endTime + 10) // enough to overshoot
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsTrue_IfTwoBigDonationsBetweenLivestreams() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(15f, this.livestream1.endTime + 10), // not enough by themselves
        createDonation(15f, this.livestream1.endTime + 15)
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfSmallDonationBetweenLivestreams() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(10f, this.livestream1.endTime + 10) // not enough to overshoot
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfDonationsDontOverlapAndTooSmall() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(15f, this.livestream1.startTime),
        createDonation(5f, this.livestream3.startTime)
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result);
  }

  @Test
  public void shouldShowDonationEffect_ReturnsFalse_IfSmallDonationsOverlap() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(10f, this.livestream1.startTime),
        createDonation(10f, this.livestream1.startTime),
        createDonation(10f, this.livestream1.startTime),
        createDonation(10f, this.livestream1.startTime)
    ));

    boolean result = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result);
  }

  @Test
  public void shouldShowDonationEffect_AutomaticallyStopsShowing() {
    when(mockDonationApiStore.getData()).thenReturn(Collections.list(
        createDonation(15f, this.livestream3.startTime)
    ));

    // shows close to donation
    boolean result1 = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertTrue(result1);

    // doesn't show far away from donation
    when(this.mockDateTimeService.now()).thenReturn(this.livestream3.startTime + 16 * 5 * 60 * 1000); // it's only valid for 15*5 minutes

    boolean result2 = this.donationService.shouldShowDonationEffect(this.userId);

    Assert.assertFalse(result2);
  }

  private static List<PublicUserRank> createRanks(RankName... rankNames) {
    return Collections.map(Collections.list(rankNames), rankName -> {
      return new PublicUserRank() {{
        rank = new PublicRank() {{
          name = rankName;
        }};
      }};
    });
  }

  private static PublicDonation createDonation(float donationAmount, long donationTime) {
    PublicUser user = new PublicUser() {{ primaryUserId = 1; }};
    return new PublicDonation() {{
      linkedUser = user;
      time = donationTime;
      linkedAt = donationTime;
      amount = donationAmount;
    }};
  }
}
