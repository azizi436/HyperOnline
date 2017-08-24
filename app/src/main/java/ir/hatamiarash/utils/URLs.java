/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.utils;

public class URLs {
    //private static final String IP = "hyper-online.ir";
//    private static final String IP = "192.168.1.104";
    private static final String IP = "192.168.1.5";
    private static final String API = "api/v1/";
    //public static final String base_URL = "http://zimia.ir/client.php";
    public static final String base_URL = "http://" + IP + "/" + API;
    //public static final String image_URL = "http://zimia.ir/images/";
    public static final String image_URL = "http://" + IP + "/images/";
    
    public static final String Check_URL = "https://pardano.com/p/mobilepayment/" + TAGs.API_KEY;
    public static final String Pay_URL = "https://pardano.com/p/payment/";
    public static final String Verify_URL = "https://pardano.com/p/mobileverify/";
}
