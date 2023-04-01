/*
 * twinder
 * CS6650 assignment API
 *
 * OpenAPI spec version: 1.2
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches
 */

public class Matches {
  @SerializedName("matchList")
  private List<String> matchList = null;

  public Matches matchList(List<String> matchList) {
    this.matchList = matchList;
    return this;
  }

  public Matches addMatchListItem(String matchListItem) {
    if (this.matchList == null) {
      this.matchList = new ArrayList<String>();
    }
    this.matchList.add(matchListItem);
    return this;
  }

  /**
   * Get matchList
   * @return matchList
   **/
  public List<String> getMatchList() {
    return matchList;
  }

  public void setMatchList(List<String> matchList) {
    this.matchList = matchList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Matches matches = (Matches) o;
    return Objects.equals(this.matchList, matches.matchList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchList);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Matches {\n");

    sb.append("    matchList: ").append(toIndentedString(matchList)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}