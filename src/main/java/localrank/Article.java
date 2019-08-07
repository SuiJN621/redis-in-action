package localrank;

import lombok.Data;

/**
 * @author Sui
 * @date 2019.08.06 10:15
 */
@Data
public class Article {
    private String title;
    private String poster;
    private String link;
    private Integer votes;
    private Integer devotes;
    private Double time;
}
