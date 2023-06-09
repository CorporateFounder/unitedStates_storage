package International_Trade_Union.governments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    //количество юридических или физических лиц в данной должности
    //количество юридических или физических лиц в данной должности
    private String name;
    private  int count;

    private boolean electedByCEO;
    private boolean electedByBoardOfDirectors;


    private boolean electedByCorporateCouncilOfReferees;
    private boolean electedByStocks;
    private boolean officeOfDirectors;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Director)) return false;
        Director director = (Director) o;
        return getName().equals(director.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public boolean isAppointedByTheGovernment(){
        if(electedByCEO || electedByBoardOfDirectors || electedByCorporateCouncilOfReferees)
            return true;
        else return false;
    }
    public int getCount() {
        return count;
    }




}
