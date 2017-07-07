/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

public class Comment {
    public String uid;
    public String name;
    public String tid;
    public int type;
    public String body;
    public String answer;
    public String date;

    public Comment(String uid, String name, String tid, int type, String body, String answer, String date) {
        this.uid = uid;
        this.name = name;
        this.tid = tid;
        this.type = type;
        this.body = body;
        this.answer = answer;
        this.date = date;
    }
}