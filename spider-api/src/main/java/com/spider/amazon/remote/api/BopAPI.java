package com.spider.amazon.remote.api;

/**
 * @ClassName BopAPI
 * @Description BOP API路径类
 */
public class BopAPI {

    // VC PO信息获取
    public static final String VC_PO_INFO = "/TPA/api/AmazonVenodrInputPO/GetAmazonVendorInputPO?pageNo={pageNo}&pageSize={pageSize}&EnterDate={enterDate}&LastUpdate={lastUpdate}&ASIN={asin}&PONum={poNum}&Vendor={vendor}";

    // PO HEADER信息获取
    public static final String GET_PO_HEADER = "/TPA/API/POHeader/getPOHeader?pageNo={pageNo}&pageSize={pageSize}&asin={asin}&poDate={poDate}";

    // WAREHOUSE TRANSFER信息获取
    public static final String GET_WAREHOUSE_TRANSFER = "/TPA/API/WarehouseTransfer/getWarehouseTransfer?pageNo={pageNo}&pageSize={pageSize}";

    // INVENTORY DATA信息获取
    public static final String GET_INVENTORY_DATA_DAILY_SNAPSHOT = "/TPA/api/InventoryData/getInventoryDataDailySnapShot?PageNo={PageNo}&PageSize={PageSize}";

    // INVOICE信息获取
    public static final String GET_INVOICE = "/TPA/API/InvoiceHeader/getInvoice?pageNo={pageNo}&pageSize={pageSize}&asin={asin}&channel={channel}&invoiceDate={invoiceDate}";


}
