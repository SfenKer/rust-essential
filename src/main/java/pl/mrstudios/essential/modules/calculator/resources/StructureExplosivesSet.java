package pl.mrstudios.essential.modules.calculator.resources;

public class StructureExplosivesSet {

    public String id;
    public String name;
    public Long emoji;
    public Structure[] structures;

    public static class Structure {

        public String id;
        public String name;
        public Long emoji;
        public Costs costs;

        public static class Costs {
            public Integer rocket;
            public Integer bomb;
            public Integer satchel;
            public Integer explosive;
        }

    }

}
