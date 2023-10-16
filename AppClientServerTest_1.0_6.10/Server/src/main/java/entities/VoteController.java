package entities;

import java.io.Serializable;
import java.util.HashSet;

public class VoteController implements Serializable {
    private HashSet<Topic> topics;

    public VoteController() {
        this.topics = new HashSet<>();
    }

    public HashSet<Topic> getTopics() {
        return topics;
    }

    public void setTopics(HashSet<Topic> topics) {
        this.topics = topics;
    }

    public void addTopic(Topic topic){
        this.topics.add(topic);
    }
    public Topic findTopicByName(String nameOfTopic){
        return topics.stream()
                .filter(topic1 -> nameOfTopic.equals(topic1.getNameOfTopic()))
                .findAny()
                .orElse(null);
    }
}
