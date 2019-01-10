package com.ticketmaster.presence;
/*
    Copyright 2019 Ticketmaster

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

final class EntryData {

    private final String barcode;
    private final String token;
    private final String customerKey;
    private final String eventKey;

    EntryData(String barcode, String token, String customerKey, String eventKey) {
        this.barcode = barcode;
        this.token = token;
        this.customerKey = customerKey;
        this.eventKey = eventKey;
    }

    EntryData(String barcode) {
        this.barcode = barcode;
        this.token = null;
        this.customerKey = null;
        this.eventKey = null;
    }

    String getBarcode() {
        return barcode;
    }

    String getToken() {
        return token;
    }

    String getCustomerKey() {
        return customerKey;
    }

    String getEventKey() {
        return eventKey;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntryData entryData = (EntryData) o;

        if (!barcode.equals(entryData.barcode)) return false;
        if (token != null ? !token.equals(entryData.token) : entryData.token != null) return false;
        if (customerKey != null ? !customerKey.equals(entryData.customerKey) : entryData.customerKey != null)
            return false;
        return eventKey != null ? eventKey.equals(entryData.eventKey) : entryData.eventKey == null;
    }

    @Override
    public int hashCode() {
        int result = barcode != null ? barcode.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (customerKey != null ? customerKey.hashCode() : 0);
        result = 31 * result + (eventKey != null ? eventKey.hashCode() : 0);
        return result;
    }
}
