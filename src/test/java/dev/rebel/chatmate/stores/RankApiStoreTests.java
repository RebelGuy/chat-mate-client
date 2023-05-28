package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.models.rank.GetUserRanksResponse.GetUserRanksResponseData;
import dev.rebel.chatmate.api.proxy.RankEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RankApiStoreTests {
  @Mock RankEndpointProxy rankEndpointProxy;
  RankApiStore rankApiStore;

  @Before
  public void setup() {
    this.rankApiStore = new RankApiStore(this.rankEndpointProxy);
  }

  @Test
  public void getUserRanksAtTime_includesRanksIfTimeInCorrectRange() {
    int userId = 5;
    long time = 1000;
    this.rankApiStore.loadUserRanks(userId, x -> {}, x -> {}, true);

    // mock the server response
    ArgumentCaptor<Consumer<GetUserRanksResponseData>> callback = ArgumentCaptor.forClass(Consumer.class);
    verify(this.rankEndpointProxy).getRanksAsync(eq(userId), eq(true), callback.capture(), any());
    callback.getValue().accept(new GetUserRanksResponseData() {{
      ranks = new PublicUserRank[] {
          new PublicUserRank() {{ issuedAt = time - 5; }},
          new PublicUserRank() {{ issuedAt = time - 5; expirationTime = time + 5; }},
          new PublicUserRank() {{ issuedAt = time - 5; expirationTime = time + 5; }}
      };
    }});

    // test before
    List<PublicUserRank> result1 = this.rankApiStore.getUserRanksAtTime(userId, time - 10);
    Assert.assertEquals(0, result1.size());

    // test during
    List<PublicUserRank> result2 = this.rankApiStore.getUserRanksAtTime(userId, time);
    Assert.assertEquals(3, result2.size());

    // test after
    List<PublicUserRank> result3 = this.rankApiStore.getUserRanksAtTime(userId, time + 10);
    Assert.assertEquals(1, result3.size());
  }
}
