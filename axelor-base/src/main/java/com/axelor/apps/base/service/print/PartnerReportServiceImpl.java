/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.print;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.tool.ReportTool;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class PartnerReportServiceImpl implements PartnerReportService {

  public static PartnerReportService getInstance() {
    return Beans.get(PartnerReportService.class);
  }

  @Override
  public List<Map<String, Object>> getPartnerData(Long partnerId) {
    Partner partner = Beans.get(PartnerRepository.class).find(partnerId);
    Map<String, Object> partnerDataMap = setPartnerMap(partner);
    List<PartnerAddress> partnerAddressList = partner.getPartnerAddressList();
    if (CollectionUtils.isNotEmpty(partnerAddressList)) {
      for (PartnerAddress partnerAddress : partnerAddressList) {
        if (partnerAddress.getIsDefaultAddr() && partnerAddress.getIsInvoicingAddr()) {
          setPartnerAddress(partnerDataMap, partnerAddress.getAddress());
          break;
        }
      }
    } else {
      setPartnerAddress(partnerDataMap, partner.getMainAddress());
    }
    return Lists.newArrayList(partnerDataMap);
  }

  protected Map<String, Object> setPartnerMap(Partner partner) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("name");
    fieldsList.add("firstName");
    return ReportTool.getMap(partner, fieldsList);
  }

  protected void setPartnerAddress(Map<String, Object> map, Address address) {
    if (ObjectUtils.notEmpty(address)) {
      map.put("addressL4", address.getAddressL4());
      map.put("addressL6", address.getAddressL6());
    }
  }
}
