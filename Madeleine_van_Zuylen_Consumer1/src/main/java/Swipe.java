public class Swipe {
  private String swiperId;
  private String swipeeId;
  private String direction;
  //private String comment;

  public Swipe(String swiperId, String swipeeId, String direction){
    this.swiperId = swiperId;
    this.swipeeId = swipeeId;
    this.direction = direction;
    //this.comment = comment;
  }

  public String getSwiperId() {
    return swiperId;
  }

  public String getSwipeeId() {
    return swipeeId;
  }

  public String getDirection() {
    return direction;
  }
/**
  public String getComment() {
    return comment;
  }
 */
}
