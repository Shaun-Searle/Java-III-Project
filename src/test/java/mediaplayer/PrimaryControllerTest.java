package mediaplayer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

public class PrimaryControllerTest {
    
    PrimaryController primary = new PrimaryController();

    @Before
    public void init() {

        for (int i = 0; i < 30; i++) {

            primary.playlist.add(new SongTrack(RandomStringUtils.randomAlphabetic(5), null));

        }

        primary.playlist.add(new SongTrack("ggTCZ", null));

        SongTrack[] tempArr = new SongTrack[primary.playlist.size()];

        primary.playlist.toArray(tempArr);

        primary.quickSort(tempArr);

        primary.playlist.clear();

        primary.playlist.addAll(Arrays.asList(tempArr));
        
    }

    @Test
    public void SearchPresentTest() {

        assertFalse("Item should be present", primary.binarySearch("ggTCZ") < 0); 
    }

    @Test
    public void searchNotPresentTest() {

        assertTrue("Item should not be present", primary.binarySearch("ggTCZNot") < 0); 
    }

}