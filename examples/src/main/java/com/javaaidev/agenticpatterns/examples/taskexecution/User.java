package com.javaaidev.agenticpatterns.examples.taskexecution;

import java.util.List;

public record User(String id,
                   String name,
                   String email,
                   String mobilePhone,
                   List<Address> addresses) {

  public enum AddressType {
    HOME,
    OFFICE,
    OTHER,
  }

  public record Address(
      String id,
      AddressType addressType,
      String countryOrRegion,
      String provinceOrState,
      String city,
      String addressLine,
      String zipCode) {

  }
}
