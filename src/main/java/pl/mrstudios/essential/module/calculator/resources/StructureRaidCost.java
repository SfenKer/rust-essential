package pl.mrstudios.essential.module.calculator.resources;

public class StructureRaidCost {

    public String id;
    public String name;
    public Long emoji;
    public Costs costs;

    public static class Costs {
        public Integer rocket;
        public Integer bomb;
        public Integer explosive;
    }

}
