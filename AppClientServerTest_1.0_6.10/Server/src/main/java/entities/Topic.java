package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Topic implements Serializable {
    private String nameOfTopic;
    private List<Vote> votesInTopic;

    public Topic(String nameOfTopic) {
        this.nameOfTopic = nameOfTopic;
        this.votesInTopic = new ArrayList<>();
    }

    public String getNameOfTopic() {
        return nameOfTopic;
    }

    public void setNameOfTopic(String nameOfTopic) {
        this.nameOfTopic = nameOfTopic;
    }

    public List<Vote> getVotesInTopic() {
        return votesInTopic;
    }

    public void setVotesInTopic(List<Vote> votesInTopic) {
        this.votesInTopic = votesInTopic;
    }

    public void addVote(Vote vote){
        this.votesInTopic.add(vote);
    }
    public void deleteVote(String nameOfVote, String clientName){
        Vote vote = findVoteByName(nameOfVote);
        if(vote != null){
            if(vote.getCreator().equals(clientName)){
                votesInTopic.remove(vote);
            }
        }
    }

    public Vote findVoteByName(String nameOfVote){
        return votesInTopic.stream()
                .filter(vote1 -> nameOfVote.equals(vote1.getVoteName()))
                .findAny()
                .orElse(null);
    }
}
