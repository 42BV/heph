package nl._42.heph.builder;

import java.util.Arrays;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.AncientTribe;

public class AncientTribeFixtures extends AbstractBuilder<AncientTribe, AncientTribeBuildCommand> {


    @Override
    public AncientTribeBuildCommand base() {
        return blank();
    }

    AncientTribe himba() {
        return base()
                .withInhabitantsHaveChildren(new boolean[] {true, false, true})
                .withPhoto(new byte[] {1, 0, 1, 0, 0, 0, 0, 0, 1})
                .withInhabitantChildrenCounts(new short[] {5, 0, 2})
                .withInhabitantAges(new int[] {29, 14, 24})
                .withInhabitantLengths(new long[] {169, 148, 175})
                .withInhabitantShoeSizes(new float[] {39.5f, 37, 42f})
                .withInhabitantWeights(new double[] {65, 50, 74})
                .withInhabitantInitials(new char[] {'A', 'M', 'D'})
                .withInhabitantNames(Arrays.asList("Aaron", "Mustafa", "Dinhi"))
                .withInhabitantsHaveBeenResearched(new boolean[] {true, true, true})
                .construct();
    }
}
