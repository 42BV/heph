package nl._42.heph.domain;

import java.util.Collection;

import nl._42.heph.shared.AbstractEntity;

/**
 * A primitive test object to see if building all primitive arrays is supported
 */
public class AncientTribe extends AbstractEntity {

    private boolean[] inhabitantsHaveChildren;
    private Collection<Boolean> inhabitantsHaveBeenResearched;
    private byte[] photo;
    private short[] inhabitantChildrenCounts;
    private int[] inhabitantAges;
    private long[] inhabitantLengths;
    private float[] inhabitantShoeSizes;
    private double[] inhabitantWeights;
    private char[] inhabitantInitials;
    private String[] inhabitantNames;

    public boolean[] getInhabitantsHaveChildren() {
        return inhabitantsHaveChildren;
    }

    public void setInhabitantsHaveChildren(boolean[] inhabitantsHaveChildren) {
        this.inhabitantsHaveChildren = inhabitantsHaveChildren;
    }

    public Collection<Boolean> getInhabitantsHaveBeenResearched() {
        return inhabitantsHaveBeenResearched;
    }

    public void setInhabitantsHaveBeenResearched(Collection<Boolean> inhabitantsHaveBeenResearched) {
        this.inhabitantsHaveBeenResearched = inhabitantsHaveBeenResearched;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public short[] getInhabitantChildrenCounts() {
        return inhabitantChildrenCounts;
    }

    public void setInhabitantChildrenCounts(short[] inhabitantChildrenCounts) {
        this.inhabitantChildrenCounts = inhabitantChildrenCounts;
    }

    public int[] getInhabitantAges() {
        return inhabitantAges;
    }

    public void setInhabitantAges(int[] inhabitantAges) {
        this.inhabitantAges = inhabitantAges;
    }

    public long[] getInhabitantLengths() {
        return inhabitantLengths;
    }

    public void setInhabitantLengths(long[] inhabitantLengths) {
        this.inhabitantLengths = inhabitantLengths;
    }

    public float[] getInhabitantShoeSizes() {
        return inhabitantShoeSizes;
    }

    public void setInhabitantShoeSizes(float[] inhabitantShoeSizes) {
        this.inhabitantShoeSizes = inhabitantShoeSizes;
    }

    public double[] getInhabitantWeights() {
        return inhabitantWeights;
    }

    public void setInhabitantWeights(double[] inhabitantWeights) {
        this.inhabitantWeights = inhabitantWeights;
    }

    public char[] getInhabitantInitials() {
        return inhabitantInitials;
    }

    public void setInhabitantInitials(char[] inhabitantInitials) {
        this.inhabitantInitials = inhabitantInitials;
    }

    public String[] getInhabitantNames() {
        return inhabitantNames;
    }

    public void setInhabitantNames(String[] inhabitantNames) {
        this.inhabitantNames = inhabitantNames;
    }
}
