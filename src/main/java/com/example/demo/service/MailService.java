package com.example.demo.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {
  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  private final ObjectProvider<JavaMailSender> mailSenderProvider;

  public MailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
    this.mailSenderProvider = mailSenderProvider;
  }

  public void send(String to, String subject, String body) {
    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      log.info("JavaMailSender が未設定のため、メール送信をスキップします。 subject={}, to={}", subject, to);
      return;
    }
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(body);
    mailSender.send(msg);
  }

  // ★ 追加：UserController から呼ばれているメソッド
  public void sendResetPasswordMail(String to, String resetUrl) {
    String subject = "【美女単】パスワード再設定のご案内";
    String body = ""
        + "パスワード再設定の手続きが行われました。\n"
        + "以下のリンクから再設定を完了してください。\n\n"
        + resetUrl + "\n\n"
        + "※このリンクには有効期限があります。心当たりが無い場合は本メールを破棄してください。\n";
    send(to, subject, body);
  }
}
