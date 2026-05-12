package com.example.website.citu.entity;



import lombok.Getter;
import lombok.Setter;



import java.util.List;


@Getter
@Setter
public class EntityLaws {




    private EntityDtoTransaction entityDtoTransaction;

    private Long id;

    boolean lawsIsNull;

    String packetLawName;



    List<String> laws;
    //
//    @Lob
//    String laws;
    String hashLaw;

    public EntityLaws() {
    }

    public EntityLaws( boolean lawsIsNull, String packetLawName, List<String> laws, String hashLaw) {
        this.lawsIsNull = lawsIsNull;
        this.packetLawName = packetLawName;
        this.laws = laws;
        this.hashLaw = hashLaw;
    }

    @Override
    public String toString() {
        return "EntityLaws{" +
                ", id=" + id +
                ", packetLawName='" + packetLawName + '\'' +
                ", laws=" + laws +
                ", isNull=" + lawsIsNull +
                ", hashLaw='" + hashLaw + '\'' +
                '}';
    }
}
