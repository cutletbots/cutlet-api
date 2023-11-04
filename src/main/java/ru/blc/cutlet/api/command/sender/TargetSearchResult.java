package ru.blc.cutlet.api.command.sender;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import ru.blc.cutlet.api.bean.ChatUser;

@Accessors(fluent = true, chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class TargetSearchResult {
    /**
     * Found target
     */
    ChatUser target;
    FindCase findCase;
    /**
     * Text that contains found target<br/>
     * Got by {@code string.substring(this.startIndex, this.endIndex)}
     */
    String targetText;
    /**
     * If target is in text message - start index of target
     */
    int startIndex;
    /**
     * If target is in text message - end index of target
     */
    int endIndex;

    public TargetSearchResult(TargetSearchResult copy){
        this.target(copy.target())
                .findCase(copy.findCase())
                .targetText(copy.targetText())
                .startIndex(copy.startIndex())
                .endIndex(copy.endIndex());
    }


    /**
     * Found way
     */
    public enum FindCase {
        /**
         * By forwarded message
         */
        FORWARD,
        /**
         * By mention
         */
        MENTION,
        /**
         * Something else
         */
        OTHER,
        ;
    }
}
