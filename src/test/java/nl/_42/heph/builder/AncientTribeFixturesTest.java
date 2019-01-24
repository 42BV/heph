package nl._42.heph.builder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import nl._42.heph.domain.AncientTribe;

import org.junit.Test;

public class AncientTribeFixturesTest {


    @Test
    public void testPrimitiveArrayMappings() {
        AncientTribe tribe = new AncientTribeFixtures().himba();

        assertArrayEquals(new boolean[] {true, false, true}, tribe.getInhabitantsHaveChildren());
        assertArrayEquals(new short[] {5, 0, 2}, tribe.getInhabitantChildrenCounts());
        assertArrayEquals(new int[] {29, 14, 24}, tribe.getInhabitantAges());
        assertArrayEquals(new long[] {169, 148, 175}, tribe.getInhabitantLengths());
        assertArrayEquals(new float[] {39.5f, 37, 42f}, tribe.getInhabitantShoeSizes(), 0.01f);
        assertArrayEquals(new double[] {65, 50, 74}, tribe.getInhabitantWeights(), 0.01d);
        assertArrayEquals(new char[] {'A', 'M', 'D'}, tribe.getInhabitantInitials());
        assertArrayEquals(new String[] {"Aaron", "Mustafa", "Dinhi"}, tribe.getInhabitantNames());
        assertTrue(tribe.getInhabitantsHaveBeenResearched().stream().allMatch(b -> b));
    }
}
