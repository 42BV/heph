package nl._42.heph.builder;

import java.util.Collection;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.NoOpBeanSaver;
import nl._42.heph.domain.AncientTribe;

public interface AncientTribeBuildCommand extends AbstractBuildCommand<AncientTribe, NoOpBeanSaver> {

    @Override
    default AncientTribe findEntity(AncientTribe ancientTribe) {
        return null; // Back in the past, re-using of resources was not yet invented...
    }

    // Primitive array -> Primitive array (all possible types)
    AncientTribeBuildCommand withInhabitantsHaveChildren(boolean[] inhabitantsHaveChildren);

    AncientTribeBuildCommand withPhoto(byte[] photoContents);

    AncientTribeBuildCommand withInhabitantChildrenCounts(short[] inhabitantChildrenCounts);

    AncientTribeBuildCommand withInhabitantAges(int[] inhabitantAges);

    AncientTribeBuildCommand withInhabitantLengths(long[] inhabitantLengths);

    AncientTribeBuildCommand withInhabitantShoeSizes(float[] inhabitantShoeSizes);

    AncientTribeBuildCommand withInhabitantWeights(double[] inhabitantWeights);

    AncientTribeBuildCommand withInhabitantInitials(char[] inhabitantInitials);

    // Collection -> Array
    AncientTribeBuildCommand withInhabitantNames(Collection<String> inhabitantNames);

    // Primitive array -> Collection
    AncientTribeBuildCommand withInhabitantsHaveBeenResearched(boolean[] inhabitantsAreResearched);
}
