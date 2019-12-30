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
package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.base.db.BankAddress;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.sale.db.CustomerCatalog;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderReportServiceImpl implements SaleOrderReportService {

  public static SaleOrderReportService getInstance() {
    return Beans.get(SaleOrderReportService.class);
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);

    List<Map<String, Object>> dataMapList = new ArrayList<>();
    Map<String, Object> saleOrderDataMap = setOrderLineSaleOrderDataMap(saleOrder);
    saleOrderDataMap.put("currencyCode", saleOrder.getCurrency().getCode());

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        Map<String, Object> saleOrderLineDataMap = setOrderLineSaleOrderLineDataMap(saleOrderLine);

        if (ObjectUtils.notEmpty(saleOrderLine.getEstimatedDelivDate())) {
          saleOrderLineDataMap.put(
              "estimatedDeliveryDate", DateTool.toDate(saleOrderLine.getEstimatedDelivDate()));
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getUnit())) {
          saleOrderLineDataMap.put("unitCode", saleOrderLine.getUnit().getLabelToPrinting());
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getTaxLine())) {
          saleOrderLineDataMap.put("taxLine", saleOrderLine.getTaxLine().getValue());
        }

        BigDecimal unitPrice =
            saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
        saleOrderLineDataMap.put("unitPrice", unitPrice);

        BigDecimal totalDiscountAmount =
            saleOrderLine.getPriceDiscounted().subtract(unitPrice).multiply(saleOrderLine.getQty());
        saleOrderLineDataMap.put("totalDiscountAmount", totalDiscountAmount);

        Boolean isTitleLine =
            saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_TITLE);
        saleOrderLineDataMap.put("isTitleLine", isTitleLine);
        SaleOrderLine packHideUnitAmountsLine =
            Beans.get(SaleOrderLineRepository.class)
                .all()
                .filter(
                    "self.saleOrder = ?1 AND self.typeSelect = ?2 AND self.sequence > ?3 ORDER BY self.sequence",
                    saleOrder,
                    SaleOrderLineRepository.TYPE_TITLE,
                    saleOrderLine.getSequence())
                .fetchOne();
        if (ObjectUtils.notEmpty(packHideUnitAmountsLine)) {
          saleOrderLineDataMap.put(
              "packHideUnitAmounts", packHideUnitAmountsLine.getIsHideUnitAmounts());
        }

        Product product = saleOrderLine.getProduct();
        if (ObjectUtils.notEmpty(product)) {
          saleOrderLineDataMap.putAll(getMap(product, "productCode", "productTypeSelect"));
          if (ObjectUtils.notEmpty(product.getPicture())) {
            saleOrderLineDataMap.put("productPicture", product.getPicture().getFilePath());
          }
          if (CollectionUtils.isNotEmpty(product.getCustomerCatalogList())) {
            List<CustomerCatalog> customerCatalogList =
                product
                    .getCustomerCatalogList()
                    .stream()
                    .filter(
                        customerCatalog ->
                            customerCatalog
                                .getCustomerPartner()
                                .equals((saleOrder.getClientPartner())))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(customerCatalogList)) {
              for (CustomerCatalog customerCatalog : customerCatalogList) {
                Map<String, Object> customerCatalogMap =
                    setOrderLineCustomerCatalogDataMap(customerCatalog);
                customerCatalogMap.putAll(saleOrderLineDataMap);
                customerCatalogMap.putAll(saleOrderDataMap);
                dataMapList.add(customerCatalogMap);
              }
            } else {
              saleOrderLineDataMap.putAll(saleOrderDataMap);
              dataMapList.add(saleOrderLineDataMap);
            }
          }
        }
      }
    } else {
      dataMapList.add(saleOrderDataMap);
    }
    return dataMapList;
  }

  protected Map<String, Object> setOrderLineSaleOrderDataMap(SaleOrder saleOrder) {
    return getMap(saleOrder, "inAti");
  }

  protected Map<String, Object> setOrderLineSaleOrderLineDataMap(SaleOrderLine saleOrderLine) {
    return getMap(
        saleOrderLine,
        "id",
        "description",
        "quantity",
        "productName",
        "exTaxTotal",
        "inTaxTotal",
        "sequence",
        "priceDiscounted",
        "showTotal",
        "hideUnitAmounts");
  }

  protected Map<String, Object> setOrderLineCustomerCatalogDataMap(
      CustomerCatalog customerCatalog) {
    return getMap(customerCatalog, "productCustomerCode", "productCustomerName");
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineTaxData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    List<SaleOrderLineTax> saleOrderLineTaxList = saleOrder.getSaleOrderLineTaxList();
    if (CollectionUtils.isNotEmpty(saleOrderLineTaxList)) {
      for (SaleOrderLineTax saleOrderLineTax : saleOrderLineTaxList) {
        Map<String, Object> dataMap = setSaleOrderLineTaxDataMap(saleOrderLineTax);
        if (ObjectUtils.notEmpty(saleOrderLineTax.getTaxLine())) {
          dataMap.put("value", saleOrderLineTax.getTaxLine().getValue());
        }
        dataMapList.add(dataMap);
      }
    }
    return dataMapList;
  }

  protected Map<String, Object> setSaleOrderLineTaxDataMap(SaleOrderLineTax saleOrderLineTax) {
    return getMap(saleOrderLineTax, "exTaxBase", "taxTotal");
  }

  @Override
  public List<Map<String, Object>> getSaleOrderData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    Map<String, Object> dataMap = setSaleOrderDataMap(saleOrder);

    if (ObjectUtils.notEmpty(saleOrder.getCreationDate())) {
      dataMap.put("creationDate", DateTool.toDate(saleOrder.getCreationDate()));
    }
    if (ObjectUtils.notEmpty(saleOrder.getDeliveryDate())) {
      dataMap.put("deliveryDate", DateTool.toDate(saleOrder.getDeliveryDate()));
    }
    if (ObjectUtils.notEmpty(saleOrder.getEndOfValidityDate())) {
      dataMap.put("endOfValidityDate", DateTool.toDate(saleOrder.getEndOfValidityDate()));
    }

    User salespersonUser = saleOrder.getSalespersonUser();
    if (ObjectUtils.notEmpty(salespersonUser)) {
      if (ObjectUtils.notEmpty(salespersonUser.getElectronicSignature())) {
        dataMap.put(
            "salespersonSignaturePath", salespersonUser.getElectronicSignature().getFilePath());
      }
      Partner salesmanPartner = salespersonUser.getPartner();
      if (ObjectUtils.notEmpty(salesmanPartner)) {
        dataMap.put(
            "salemanName", salesmanPartner.getName() + " " + salesmanPartner.getFirstName());
        dataMap.put("salemanPhone", salesmanPartner.getFixedPhone());
        if (ObjectUtils.notEmpty(salesmanPartner.getEmailAddress())) {
          dataMap.put("salemanEmail", salesmanPartner.getEmailAddress().getAddress());
        }
      }
    }

    Company company = saleOrder.getCompany();
    dataMap.put("companyName", company.getName());
    dataMap.put("logoHeight", company.getHeight());
    dataMap.put("logoWidth", company.getWidth());

    SaleConfig saleConfig = company.getSaleConfig();
    if (ObjectUtils.notEmpty(saleConfig)) {
      dataMap.putAll(
          getMap(
              saleConfig,
              "displaySalemanOnPrinting",
              "saleOrderClientBox",
              "saleOrderLegalNote",
              "displayDelCondOnPrinting",
              "displayProductCodeOnPrinting",
              "displayTaxDetailOnPrinting",
              "displayEstimDelivDateOnPrinting",
              "displayCustomerCodeOnPrinting",
              "displayProductPictureOnPrinting"));
    }

    MetaFile companyLogo = company.getLogo();
    if (ObjectUtils.notEmpty(saleOrder.getTradingName())) {
      MetaFile tradingLogo = saleOrder.getTradingName().getLogo();
      if (ObjectUtils.notEmpty(tradingLogo)) {
        dataMap.put("logoPath", tradingLogo.getFilePath());
      }
    } else if (ObjectUtils.notEmpty(companyLogo)) {
      dataMap.put("logoPath", companyLogo.getFilePath());
    }

    Partner clientPartner = saleOrder.getClientPartner();
    dataMap.put("customerCode", clientPartner.getPartnerSeq());
    dataMap.put("partnerTypeSelect", clientPartner.getPartnerTypeSelect());
    dataMap.put("clientPartName", clientPartner.getName());
    dataMap.put("clientPartFirstName", clientPartner.getFirstName());
    dataMap.put("clientTitle", clientPartner.getTitleSelect());

    Partner contactPartner = saleOrder.getContactPartner();
    if (ObjectUtils.notEmpty(contactPartner)) {
      dataMap.put("contactName", contactPartner.getName());
      dataMap.put("contactFirstName", contactPartner.getFirstName());
    }

    if (ObjectUtils.notEmpty(saleOrder.getMainInvoicingAddress())) {
      Country invoicingCountry = saleOrder.getMainInvoicingAddress().getAddressL7Country();
      dataMap.put("invoiceCountry", invoicingCountry.getName());
    }
    if (ObjectUtils.notEmpty(saleOrder.getDeliveryAddress())) {
      Country deliveryCountry = saleOrder.getDeliveryAddress().getAddressL7Country();
      dataMap.put("deliveryCountry", deliveryCountry.getName());
    }

    BankDetails bankDetails = saleOrder.getCompanyBankDetails();
    if (ObjectUtils.notEmpty(bankDetails)) {
      dataMap.put("iban", bankDetails.getIban());
      BankAddress bankAddress = bankDetails.getBankAddress();
      if (ObjectUtils.notEmpty(bankAddress)) {
        if (ObjectUtils.notEmpty(bankAddress.getAddress())) {
          dataMap.put("bankAddress", bankAddress.getAddress());
        }
      }
      dataMap.put("bic", bankDetails.getBank().getCode());
    }

    dataMap.put("currencyCode", saleOrder.getCurrency().getCode());

    PrintingSettings printingSettings =
        ObjectUtils.notEmpty(saleOrder.getPrintingSettings())
            ? saleOrder.getPrintingSettings()
            : company.getPrintingSettings();
    if (ObjectUtils.notEmpty(printingSettings)) {
      dataMap.put("header", printingSettings.getPdfHeader());
      dataMap.put("footer", printingSettings.getPdfFooter());
      dataMap.put("logoPosition", printingSettings.getLogoPositionSelect());
    }

    if (ObjectUtils.notEmpty(saleOrder.getDuration())) {
      dataMap.put("durationName", saleOrder.getDuration().getName());
    }
    return Lists.newArrayList(dataMap);
  }

  protected Map<String, Object> setSaleOrderDataMap(SaleOrder saleOrder) {
    return getMap(
        saleOrder,
        "id",
        "saleOrderSeq",
        "invoicingAddress",
        "deliveryAddress",
        "exTaxTotal",
        "taxTotal",
        "inTaxTotal",
        "externalReference",
        "description",
        "deliveryCondition",
        "hideDiscount",
        "statusSelect",
        "specificNotes",
        "versionNumber",
        "periodicityTypeSelect",
        "numberOfPeriods",
        "subscriptionText",
        "inAti",
        "proformaComments");
  }

  @Override
  public int getAppBase() {
    return Beans.get(AppBaseRepository.class).all().fetchOne().getNbDecimalDigitForUnitPrice();
  }

  public static Map<String, Object> getMap(Object model, String... fields) {
    if (model == null) {
      return null;
    }
    final Map<String, Object> map = new HashMap<>();
    final Mapper mapper = Mapper.of(model.getClass());
    List<String> fieldsList = Arrays.asList(fields);
    for (Property p : mapper.getProperties()) {
      if (fieldsList.contains(p.getName()) && ObjectUtils.notEmpty(p.get(model))) {
        map.put(p.getName(), p.get(model));
      }
    }
    return map;
  }
}
