package com.wormz.penumbraweb.restful;


/**
 * Created by markanthonypanizales on 4/13/15.
 */
public class Hello {
    private String id;
    private String name;

    public Hello() {
    }

    public Hello(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new StringBuffer(" Id: ").append(this.id).append(" Name: ").append(this.name).toString();
    }
}
