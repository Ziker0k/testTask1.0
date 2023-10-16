package entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vote implements Serializable {
    private String voteName;
    private String creator;
    private String description;
    private Map<String, Integer> option;
    private Set<String> voters;

    public Vote(String voteName, String creator, String description, Map<String, Integer> option) {
        this.voteName = voteName;
        this.creator = creator;
        this.description = description;
        this.option = option;
        this.voters = new HashSet<>();
    }

    public String getVoteName() {
        return voteName;
    }

    public void setVoteName(String voteName) {
        this.voteName = voteName;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Integer> getOption() {
        return option;
    }

    public void setOption(Map<String, Integer> option) {
        this.option = option;
    }

    public Set<String> getVoters() {
        return voters;
    }

    public void setVoters(Set<String> voters) {
        this.voters = voters;
    }

    public void addVote(String key){
        Integer valueNew = this.option.get(key);
        valueNew++;
        this.option.replace(key, valueNew);
    }
    public void addVoter(String nameOfVoter){
        this.voters.add(nameOfVoter);
    }
}
