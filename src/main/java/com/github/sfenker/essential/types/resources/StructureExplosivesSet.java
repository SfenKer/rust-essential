package com.github.sfenker.essential.types.resources;

public class StructureExplosivesSet {

    public String id;
    public String name;
    public Structure[] structures;

    public static class Structure {

        public String id;
        public String name;
        public Costs costs;

        public static class Costs {
            public Integer rocket;
            public Integer bomb;
            public Integer satchel;
            public Integer explosive;
        }

    }

}
