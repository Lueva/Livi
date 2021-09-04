package se.kry.codetest;

import java.util.Objects;

public class Service {
  private String name;
  private String url;
  private String creationDate;

  public Service(String url, String creationDate, String name) {
    this.name = name;
    this.url = url;
    this.creationDate = creationDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Service service = (Service) o;
    return url.equals(service.url) && creationDate.equals(service.creationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, creationDate);
  }
}



