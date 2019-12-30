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
package com.axelor.apps.supplychain.print;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderReportServiceImpl;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.common.ObjectUtils;
import java.util.Map;

public class SaleOrderReportServiceSupplychainImpl extends SaleOrderReportServiceImpl {

  @Override
  protected Map<String, Object> setOrderLineSaleOrderDataMap(SaleOrder saleOrder) {
    Map<String, Object> map = super.setOrderLineSaleOrderDataMap(saleOrder);
    map.put("saleOrderTypeSelect", saleOrder.getSaleOrderTypeSelect());
    return map;
  }

  @Override
  protected Map<String, Object> setSaleOrderLineTaxDataMap(SaleOrderLineTax saleOrderLineTax) {
    Map<String, Object> map = super.setSaleOrderLineTaxDataMap(saleOrderLineTax);
    map.put("saleOrderTypeSelect", saleOrderLineTax.getSaleOrder().getSaleOrderTypeSelect());
    return map;
  }

  @Override
  protected Map<String, Object> setSaleOrderDataMap(SaleOrder saleOrder) {
    Map<String, Object> map = super.setSaleOrderDataMap(saleOrder);
    map.put("saleOrderTypeSelect", saleOrder.getSaleOrderTypeSelect());
    map.put("isIspmRequired", saleOrder.getIsIspmRequired());
    if (ObjectUtils.notEmpty(saleOrder.getShipmentDate())) {
      map.put("shipmentDate", DateTool.toDate(saleOrder.getShipmentDate()));
    }

    if (ObjectUtils.notEmpty(saleOrder.getPaymentCondition())) {
      map.put("paymentCondName", saleOrder.getPaymentCondition().getName());
    }
    if (ObjectUtils.notEmpty(saleOrder.getPaymentMode())) {
      map.put("paymentMode", saleOrder.getPaymentMode().getName());
    }
    return map;
  }
}
