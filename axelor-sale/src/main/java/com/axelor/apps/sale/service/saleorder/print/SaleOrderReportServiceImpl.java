/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.AppBase;
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
import com.axelor.apps.tool.ReportTool;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaleOrderReportServiceImpl implements SaleOrderReportService {

  @Inject protected SaleOrderRepository saleOrderRepo;
  @Inject protected AppBaseRepository appBaseRepo;

  public static SaleOrderReportService getInstance() {
    return Beans.get(SaleOrderReportService.class);
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineData(Long saleOrderId) {
    SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);
    List<String> productList = new ArrayList<>();
    productList.add("code");
    productList.add("productTypeSelect");

    List<Map<String, Object>> dataMapList = new ArrayList<>();
    Map<String, Object> saleOrderDataMap = setOrderLineSaleOrderDataMap(saleOrder);
    saleOrderDataMap.put("currencyCode", saleOrder.getCurrency().getCode());

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (ObjectUtils.notEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        Map<String, Object> saleOrderLineDataMap = setOrderLineSaleOrderLineDataMap(saleOrderLine);

        ReportTool.setDateMap(
            "estimatedDeliveryDate", saleOrderLineDataMap, saleOrderLine.getEstimatedDelivDate());

        if (saleOrderLine.getUnit() != null) {
          saleOrderLineDataMap.put("unitCode", saleOrderLine.getUnit().getLabelToPrinting());
        }

        if (saleOrderLine.getTaxLine() != null) {
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

        Product product = saleOrderLine.getProduct();
        if (product != null) {
          saleOrderLineDataMap.putAll(ReportTool.getMap(product, productList));
          if (product.getPicture() != null) {
            saleOrderLineDataMap.put("productPicture", product.getPicture().getFilePath());
          }
          List<CustomerCatalog> productCustomerCatalogList = product.getCustomerCatalogList();
          if (ObjectUtils.notEmpty(productCustomerCatalogList)) {
            List<CustomerCatalog> customerCatalogList =
                productCustomerCatalogList.stream()
                    .filter(
                        customerCatalog ->
                            customerCatalog
                                .getCustomerPartner()
                                .equals((saleOrder.getClientPartner())))
                    .collect(Collectors.toList());
            if (ObjectUtils.notEmpty(customerCatalogList)) {
              for (CustomerCatalog customerCatalog : customerCatalogList) {
                Map<String, Object> customerCatalogMap =
                    setOrderLineCustomerCatalogDataMap(customerCatalog);
                customerCatalogMap.putAll(saleOrderLineDataMap);
                customerCatalogMap.putAll(saleOrderDataMap);
                dataMapList.add(customerCatalogMap);
              }
              continue;
            }
          }
        }
        saleOrderLineDataMap.putAll(saleOrderDataMap);
        dataMapList.add(saleOrderLineDataMap);
      }
      return dataMapList;
    }
    dataMapList.add(saleOrderDataMap);
    return dataMapList;
  }

  protected Map<String, Object> setOrderLineSaleOrderDataMap(SaleOrder saleOrder) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("inAti");
    return ReportTool.getMap(saleOrder, fieldsList);
  }

  protected Map<String, Object> setOrderLineSaleOrderLineDataMap(SaleOrderLine saleOrderLine) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("id");
    fieldsList.add("description");
    fieldsList.add("qty");
    fieldsList.add("productName");
    fieldsList.add("exTaxTotal");
    fieldsList.add("inTaxTotal");
    fieldsList.add("sequence");
    fieldsList.add("priceDiscounted");
    fieldsList.add("showTotal");
    fieldsList.add("hideUnitAmounts");
    return ReportTool.getMap(saleOrderLine, fieldsList);
  }

  protected Map<String, Object> setOrderLineCustomerCatalogDataMap(
      CustomerCatalog customerCatalog) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("productCustomerCode");
    fieldsList.add("productCustomerName");
    return ReportTool.getMap(customerCatalog, fieldsList);
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineTaxData(Long saleOrderId) {
    SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    List<SaleOrderLineTax> saleOrderLineTaxList = saleOrder.getSaleOrderLineTaxList();
    if (ObjectUtils.notEmpty(saleOrderLineTaxList)) {
      for (SaleOrderLineTax saleOrderLineTax : saleOrderLineTaxList) {
        Map<String, Object> dataMap = setSaleOrderLineTaxDataMap(saleOrderLineTax);
        if (saleOrderLineTax.getTaxLine() != null) {
          dataMap.put("value", saleOrderLineTax.getTaxLine().getValue());
        }
        dataMapList.add(dataMap);
      }
    }
    return dataMapList;
  }

  protected Map<String, Object> setSaleOrderLineTaxDataMap(SaleOrderLineTax saleOrderLineTax) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("exTaxBase");
    fieldsList.add("taxTotal");
    return ReportTool.getMap(saleOrderLineTax, fieldsList);
  }

  @Override
  public List<Map<String, Object>> getSaleOrderData(Long saleOrderId) {
    SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);
    Map<String, Object> dataMap = setSaleOrderDataMap(saleOrder);

    ReportTool.setDateMap("creationDate", dataMap, saleOrder.getCreationDate());
    ReportTool.setDateMap("deliveryDate", dataMap, saleOrder.getDeliveryDate());
    ReportTool.setDateMap("endOfValidityDate", dataMap, saleOrder.getEndOfValidityDate());

    User salespersonUser = saleOrder.getSalespersonUser();
    if (salespersonUser != null) {
      if (salespersonUser.getElectronicSignature() != null) {
        dataMap.put(
            "salespersonSignaturePath", salespersonUser.getElectronicSignature().getFilePath());
      }
      Partner salesmanPartner = salespersonUser.getPartner();
      if (salesmanPartner != null) {
        dataMap.put(
            "salemanName", salesmanPartner.getName() + " " + salesmanPartner.getFirstName());
        dataMap.put("salemanPhone", salesmanPartner.getFixedPhone());
        if (salesmanPartner.getEmailAddress() != null) {
          dataMap.put("salemanEmail", salesmanPartner.getEmailAddress().getAddress());
        }
      }
    }

    Company company = saleOrder.getCompany();
    dataMap.put("companyName", company.getName());
    dataMap.put("logoHeight", company.getHeight());
    dataMap.put("logoWidth", company.getWidth());

    SaleConfig saleConfig = company.getSaleConfig();
    if (saleConfig != null) {
      List<String> fieldsList = new ArrayList<>();
      fieldsList.add("displaySalemanOnPrinting");
      fieldsList.add("saleOrderClientBox");
      fieldsList.add("saleOrderLegalNote");
      fieldsList.add("displayDelCondOnPrinting");
      fieldsList.add("displayProductCodeOnPrinting");
      fieldsList.add("displayTaxDetailOnPrinting");
      fieldsList.add("displayEstimDelivDateOnPrinting");
      fieldsList.add("displayCustomerCodeOnPrinting");
      fieldsList.add("displayProductPictureOnPrinting");
      dataMap.putAll(ReportTool.getMap(saleConfig, fieldsList));
    }

    MetaFile companyLogo = company.getLogo();
    if (saleOrder.getTradingName() != null && saleOrder.getTradingName().getLogo() != null) {
      dataMap.put("logoPath", saleOrder.getTradingName().getLogo().getFilePath());
    } else if (companyLogo != null) {
      dataMap.put("logoPath", companyLogo.getFilePath());
    }

    Partner clientPartner = saleOrder.getClientPartner();
    dataMap.put("customerCode", clientPartner.getPartnerSeq());
    dataMap.put("partnerTypeSelect", clientPartner.getPartnerTypeSelect());
    dataMap.put("clientPartName", clientPartner.getName());
    dataMap.put("clientPartFirstName", clientPartner.getFirstName());
    dataMap.put("clientTitle", clientPartner.getTitleSelect());

    Partner contactPartner = saleOrder.getContactPartner();
    if (contactPartner != null) {
      dataMap.put("contactName", contactPartner.getName());
      dataMap.put("contactFirstName", contactPartner.getFirstName());
    }

    if (saleOrder.getMainInvoicingAddress() != null) {
      Country invoicingCountry = saleOrder.getMainInvoicingAddress().getAddressL7Country();
      dataMap.put("invoiceCountry", invoicingCountry.getName());
    }
    if (saleOrder.getDeliveryAddress() != null) {
      Country deliveryCountry = saleOrder.getDeliveryAddress().getAddressL7Country();
      dataMap.put("deliveryCountry", deliveryCountry.getName());
    }

    BankDetails bankDetails = saleOrder.getCompanyBankDetails();
    if (bankDetails != null) {
      dataMap.put("iban", bankDetails.getIban());
      BankAddress bankAddress = bankDetails.getBankAddress();
      if (bankAddress != null) {
        if (bankAddress.getAddress() != null) {
          dataMap.put("bankAddress", bankAddress.getAddress());
        }
      }
      dataMap.put("bic", bankDetails.getBank().getCode());
    }

    dataMap.put("currencyCode", saleOrder.getCurrency().getCode());

    PrintingSettings printingSettings =
        saleOrder.getPrintingSettings() != null
            ? saleOrder.getPrintingSettings()
            : company.getPrintingSettings();
    if (printingSettings != null) {
      dataMap.put("header", printingSettings.getPdfHeader());
      dataMap.put("footer", printingSettings.getPdfFooter());
      dataMap.put("logoPosition", printingSettings.getLogoPositionSelect());
    }

    if (saleOrder.getDuration() != null) {
      dataMap.put("durationName", saleOrder.getDuration().getName());
    }
    return Lists.newArrayList(dataMap);
  }

  protected Map<String, Object> setSaleOrderDataMap(SaleOrder saleOrder) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("id");
    fieldsList.add("saleOrderSeq");
    fieldsList.add("mainInvoicingAddressStr");
    fieldsList.add("deliveryAddressStr");
    fieldsList.add("exTaxTotal");
    fieldsList.add("taxTotal");
    fieldsList.add("inTaxTotal");
    fieldsList.add("externalReference");
    fieldsList.add("description");
    fieldsList.add("deliveryCondition");
    fieldsList.add("hideDiscount");
    fieldsList.add("statusSelect");
    fieldsList.add("versionNumber");
    fieldsList.add("periodicityTypeSelect");
    fieldsList.add("numberOfPeriods");
    fieldsList.add("subscriptionText");
    fieldsList.add("inAti");
    fieldsList.add("proformaComments");
    fieldsList.add("specificNotes");
    return ReportTool.getMap(saleOrder, fieldsList);
  }

  @Override
  public List<Map<String, Object>> getAppBaseData() {
    AppBase appBase = appBaseRepo.all().fetchOne();
    return Lists.newArrayList(setAppBaseDataMap(appBase));
  }

  protected Map<String, Object> setAppBaseDataMap(AppBase appBase) {
    List<String> fieldsList = new ArrayList<>();
    fieldsList.add("nbDecimalDigitForQty");
    fieldsList.add("nbDecimalDigitForUnitPrice");
    return ReportTool.getMap(appBase, fieldsList);
  }
}
