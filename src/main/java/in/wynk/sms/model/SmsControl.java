package in.wynk.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the parameters which are set to control the traffic. The parameters can be tuned to block messages from
 * a specific source or of a specific priority, etc.
 *
 * @author Abhishek
 * @created 06/11/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class SmsControl {
  private boolean controlTraffic = false;
  private Set<String> messages = new HashSet<>();
  private Map<String, Set<String>> prioritiesWithSource = new HashMap<>();
  private Set<String> sources = new HashSet<>();
  private Set<String> priorities = new HashSet<>();

  public boolean isControlTraffic() {
    return controlTraffic;
  }

  public void setControlTraffic(boolean controlTraffic) {
    this.controlTraffic = controlTraffic;
  }

  public Set<String> getMessages() {
    return messages;
  }

  public void setMessages(Set<String> messages) {
    this.messages = messages;
  }

  public Map<String, Set<String>> getPrioritiesWithSource() {
    return prioritiesWithSource;
  }

  public void setPrioritiesWithSource(Map<String, Set<String>> prioritiesWithSource) {
    this.prioritiesWithSource = prioritiesWithSource;
  }

  public Set<String> getSources() {
    return sources;
  }

  public void setSources(Set<String> sources) {
    this.sources = sources;
  }

  public Set<String> getPriorities() {
    return priorities;
  }

  public void setPriorities(Set<String> priorities) {
    this.priorities = priorities;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("controlTraffic", controlTraffic)
        .append("messages", messages)
        .append("prioritiesWithSource", prioritiesWithSource)
        .append("sources", sources)
        .append("priorities", priorities)
        .toString();
  }
}
