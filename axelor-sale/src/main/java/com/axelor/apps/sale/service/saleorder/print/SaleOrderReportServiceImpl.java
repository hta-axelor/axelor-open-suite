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
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    Map<String, Object> saleOrderDataMap = new HashMap<>();
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        Map<String, Object> saleOrderLineDataMap = new HashMap<>();
        saleOrderLineDataMap.put("id", saleOrderLine.getId());
        saleOrderLineDataMap.put("description", saleOrderLine.getDescription());
        saleOrderLineDataMap.put("quantity", saleOrderLine.getQty());
        saleOrderLineDataMap.put("ProductName", saleOrderLine.getProductName());
        saleOrderLineDataMap.put("ex_tax_total", saleOrderLine.getExTaxTotal());
        saleOrderLineDataMap.put("in_taxTotal", saleOrderLine.getInTaxTotal());
        saleOrderLineDataMap.put("sequence", saleOrderLine.getSequence());
        saleOrderLineDataMap.put("price_discounted", saleOrderLine.getPriceDiscounted());
        saleOrderLineDataMap.put("showTotal", saleOrderLine.getIsShowTotal());
        saleOrderLineDataMap.put("hideUnitAmounts", saleOrderLine.getIsHideUnitAmounts());

        if (ObjectUtils.notEmpty(saleOrderLine.getEstimatedDelivDate())) {
          saleOrderLineDataMap.put(
              "EstimatedDeliveryDate", DateTool.toDate(saleOrderLine.getEstimatedDelivDate()));
        }

        Product product = saleOrderLine.getProduct();
        if (ObjectUtils.notEmpty(product)) {
          saleOrderLineDataMap.put("productCode", product.getCode());
          saleOrderLineDataMap.put("product_type_select", product.getProductTypeSelect());
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
                Map<String, Object> customerCatalogMap = new HashMap<>();
                customerCatalogMap.put("CustomerProductCode", customerCatalog.getProductCustomerCode());
                customerCatalogMap.put("CustomerProductName", customerCatalog.getProductCustomerName());
                dataMapList.add(customerCatalogMap);
              }
            }
          }
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getUnit())) {
          saleOrderLineDataMap.put("UnitCode", saleOrderLine.getUnit().getLabelToPrinting());
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getTaxLine())) {
          saleOrderLineDataMap.put("tax_line", saleOrderLine.getTaxLine().getValue());
        }

        BigDecimal unitPrice =
            saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
            saleOrderLineDataMap.put("unit_price", unitPrice);

        BigDecimal totalDiscountAmount =
            saleOrderLine.getPriceDiscounted().subtract(unitPrice).multiply(saleOrderLine.getQty());
        saleOrderLineDataMap.put("totalDiscountAmount", totalDiscountAmount);

        Boolean isTitleLine =
            saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_TITLE);
        saleOrderLineDataMap.put("is_title_line", isTitleLine);
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
          saleOrderLineDataMap.put("PackHideUnitAmounts", packHideUnitAmountsLine.getIsHideUnitAmounts());
        }
        dataMapList.add(saleOrderLineDataMap);
      }
    }
    saleOrderDataMap.put("CurrencyCode", saleOrder.getCurrency().getCode());
    saleOrderDataMap.put("in_ati", saleOrder.getInAti());
    dataMapList.add(saleOrderDataMap);

    return dataMapList;
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineTaxData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();

    List<SaleOrderLineTax> saleOrderLineTaxList = saleOrder.getSaleOrderLineTaxList();
    if (CollectionUtils.isNotEmpty(saleOrderLineTaxList)) {
      for (SaleOrderLineTax saleOrderLineTax : saleOrderLineTaxList) {
        dataMap.put("ex_tax_base", saleOrderLineTax.getExTaxBase());
        dataMap.put("tax_total", saleOrderLineTax.getTaxTotal());
        if (ObjectUtils.notEmpty(saleOrderLineTax.getTaxLine())) {
          dataMap.put("value", saleOrderLineTax.getTaxLine().getValue());
        }
      }
    }
    return Lists.newArrayList(dataMap);
  }

  @Override
  public List<Map<String, Object>> getSaleOrderData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();

    dataMap.put("id", saleOrderId);
    dataMap.put("saleOrderSeq", saleOrder.getSaleOrderSeq());
    dataMap.put("invoicingAddress", saleOrder.getMainInvoicingAddressStr());
    dataMap.put("deliveryAddress", saleOrder.getDeliveryAddressStr());
    dataMap.put("ex_tax_total", saleOrder.getExTaxTotal());
    dataMap.put("tax_total", saleOrder.getTaxTotal());
    dataMap.put("in_tax_total", saleOrder.getInTaxTotal());
    dataMap.put("external_reference", saleOrder.getExternalReference());
    dataMap.put("description", saleOrder.getDescription());
    dataMap.put("deliveryCondition", saleOrder.getDeliveryCondition());
    dataMap.put("hideDiscount", saleOrder.getHideDiscount());
    dataMap.put("status_select", saleOrder.getStatusSelect());
    dataMap.put("specific_notes", saleOrder.getSpecificNotes());
    dataMap.put("versionNumber", saleOrder.getVersionNumber());
    dataMap.put("periodicity_type_select", saleOrder.getPeriodicityTypeSelect());
    dataMap.put("number_of_periods", saleOrder.getNumberOfPeriods());
    dataMap.put("subscription_text", saleOrder.getSubscriptionText());
    dataMap.put("in_ati", saleOrder.getInAti());
    dataMap.put("proforma_comments", saleOrder.getProformaComments());

    if (ObjectUtils.notEmpty(saleOrder.getCreationDate())) {
      dataMap.put("CreationDate", DateTool.toDate(saleOrder.getCreationDate()));
    }
    if (ObjectUtils.notEmpty(saleOrder.getDeliveryDate())) {
      dataMap.put("deliveryDate", DateTool.toDate(saleOrder.getDeliveryDate()));
    }
    if (ObjectUtils.notEmpty(saleOrder.getEndOfValidityDate())) {
      dataMap.put("end_of_validity_date", DateTool.toDate(saleOrder.getEndOfValidityDate()));
    }

    User salespersonUser = saleOrder.getSalespersonUser();
    if (ObjectUtils.notEmpty(salespersonUser)) {
      if (ObjectUtils.notEmpty(salespersonUser.getElectronicSignature())) {
        dataMap.put(
            "salesperson_signature_path", salespersonUser.getElectronicSignature().getFilePath());
      }
      Partner salesmanPartner = salespersonUser.getPartner();
      if (ObjectUtils.notEmpty(salesmanPartner)) {
        dataMap.put(
            "SalemanName", salesmanPartner.getName() + " " + salesmanPartner.getFirstName());
        dataMap.put("SalemanPhone", salesmanPartner.getFixedPhone());
        if (ObjectUtils.notEmpty(salesmanPartner.getEmailAddress())) {
          dataMap.put("SalemanEmail", salesmanPartner.getEmailAddress().getAddress());
        }
      }
    }

    Company company = saleOrder.getCompany();
    dataMap.put("CompanyName", company.getName());
    dataMap.put("logo_height", company.getHeight());
    dataMap.put("logo_width", company.getWidth());
    SaleConfig saleConfig = company.getSaleConfig();
    if (ObjectUtils.notEmpty(saleConfig)) {
      dataMap.put("DisplaySaleman", saleConfig.getDisplaySalemanOnPrinting());
      dataMap.put("ClientBox", saleConfig.getSaleOrderClientBox());
      dataMap.put("LegalNote", saleConfig.getSaleOrderLegalNote());
      dataMap.put("DisplayDeliveryCondition", saleConfig.getDisplayDelCondOnPrinting());
      dataMap.put("displayProductCodeOnPrinting", saleConfig.getDisplayProductCodeOnPrinting());
      dataMap.put("displayTaxDetailOnPrinting", saleConfig.getDisplayTaxDetailOnPrinting());
      dataMap.put(
          "displayEstimDelivDateOnPrinting", saleConfig.getDisplayEstimDelivDateOnPrinting());
      dataMap.put("displayCustomerCodeOnPrinting", saleConfig.getDisplayCustomerCodeOnPrinting());
      dataMap.put(
          "displayProductPictureOnPrinting", saleConfig.getDisplayProductPictureOnPrinting());
    }

    MetaFile companyLogo = company.getLogo();
    if (ObjectUtils.notEmpty(saleOrder.getTradingName())) {
      MetaFile tradingLogo = saleOrder.getTradingName().getLogo();
      if (ObjectUtils.notEmpty(tradingLogo)) {
        dataMap.put("logo_path", tradingLogo.getFilePath());
      }
    } else if (ObjectUtils.notEmpty(companyLogo)) {
      dataMap.put("logo_path", companyLogo.getFilePath());
    }

    Partner clientPartner = saleOrder.getClientPartner();
    dataMap.put("CustomerCode", clientPartner.getPartnerSeq());
    dataMap.put("partner_type_select", clientPartner.getPartnerSeq());
    dataMap.put("ClientPartName", clientPartner.getName());
    dataMap.put("ClientPartFirstName", clientPartner.getFirstName());
    dataMap.put("ClientTitle", clientPartner.getTitleSelect());

    Partner contactPartner = saleOrder.getContactPartner();
    if (ObjectUtils.notEmpty(contactPartner)) {
      dataMap.put("ContactName", contactPartner.getName());
      dataMap.put("ContactFirstName", contactPartner.getFirstName());
    }

    if (ObjectUtils.notEmpty(saleOrder.getMainInvoicingAddress())) {
      Country invoicingCountry = saleOrder.getMainInvoicingAddress().getAddressL7Country();
      dataMap.put("invoicecountry", invoicingCountry.getName());
    }
    if (ObjectUtils.notEmpty(saleOrder.getDeliveryAddress())) {
      Country deliveryCountry = saleOrder.getDeliveryAddress().getAddressL7Country();
      dataMap.put("DeliveryCountry", deliveryCountry.getName());
    }

    BankDetails bankDetails = saleOrder.getCompanyBankDetails();
    if (ObjectUtils.notEmpty(bankDetails)) {
      dataMap.put("iban", bankDetails.getIban());
      BankAddress bankAddress = bankDetails.getBankAddress();
      if (ObjectUtils.notEmpty(bankAddress)) {
        if (ObjectUtils.notEmpty(bankAddress.getAddress())) {
          dataMap.put("bank_address", bankAddress.getAddress());
        }
      }
      dataMap.put("bic", bankDetails.getBank().getCode());
    }

    dataMap.put("CurrencyCode", saleOrder.getCurrency().getCode());

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

  @Override
  public int getAppBase() {
    return Beans.get(AppBaseRepository.class).all().fetchOne().getNbDecimalDigitForUnitPrice();
  }
}
