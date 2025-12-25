/**
 * Email Service - Gửi email qua Brevo HTTP API
 * Không dùng SMTP để tránh vấn đề network trên cloud
 */
class EmailService {
  constructor() {
    this.isConfigured = false;
    this.apiKey = null;
    this.senderEmail = null;
    this.senderName = 'EcoTrack';
    this.init();
  }

  init() {
    if (process.env.BREVO_API_KEY) {
      this.apiKey = process.env.BREVO_API_KEY;
      this.senderEmail = process.env.BREVO_SENDER || process.env.SMTP_USER || 'noreply@ecotrack.app';
      this.senderName = process.env.BREVO_SENDER_NAME || 'EcoTrack';
      this.isConfigured = true;
      console.log('✅ Email service configured (Brevo API)');
      return;
    }
    
    console.log('⚠️  Email service not configured. Set BREVO_API_KEY in .env');
    console.log('   Get free API key at: https://app.brevo.com/settings/keys/api');
  }

  /**
   * Gửi email qua Brevo HTTP API
   */
  async sendEmail(to, subject, htmlContent) {
    if (!this.isConfigured) {
      return { success: false, message: 'Email service not configured' };
    }

    try {
      const response = await fetch('https://api.brevo.com/v3/smtp/email', {
        method: 'POST',
        headers: {
          'accept': 'application/json',
          'api-key': this.apiKey,
          'content-type': 'application/json'
        },
        body: JSON.stringify({
          sender: {
            name: this.senderName,
            email: this.senderEmail
          },
          to: [{ email: to }],
          subject: subject,
          htmlContent: htmlContent
        })
      });

      const data = await response.json();

      if (response.ok) {
        console.log('Email sent via Brevo API:', data.messageId);
        return { success: true, messageId: data.messageId };
      } else {
        console.error('Brevo API error:', data);
        return { success: false, message: data.message || 'Failed to send email' };
      }
    } catch (error) {
      console.error('Brevo API request failed:', error);
      return { success: false, message: error.message };
    }
  }

  /**
   * Gửi email đặt lại mật khẩu
   */
  async sendPasswordResetEmail(to, resetToken, username) {
    const subject = '🌿 EcoTrack - Mã đặt lại mật khẩu';
    const htmlContent = this.getPasswordResetTemplate(username, resetToken);
    return this.sendEmail(to, subject, htmlContent);
  }

  /**
   * Template email đặt lại mật khẩu
   */
  getPasswordResetTemplate(username, resetToken) {
    return `
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f5f5;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 20px;">
    <tr>
      <td align="center">
        <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
          <tr>
            <td style="background: linear-gradient(135deg, #2E7D32 0%, #66BB6A 100%); padding: 40px 20px; text-align: center;">
              <h1 style="color: #ffffff; margin: 0; font-size: 28px;">🌿 EcoTrack</h1>
              <p style="color: #C8E6C9; margin: 10px 0 0 0; font-size: 14px;">Sống xanh mỗi ngày</p>
            </td>
          </tr>
          <tr>
            <td style="padding: 40px 30px;">
              <h2 style="color: #1B5E20; margin: 0 0 20px 0; font-size: 24px;">Xin chào ${username || 'bạn'}! 👋</h2>
              <p style="color: #424242; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản EcoTrack của bạn.
              </p>
              <p style="color: #424242; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                Mã xác nhận của bạn là:
              </p>
              <div style="background-color: #E8F5E9; border: 2px dashed #4CAF50; border-radius: 12px; padding: 20px; text-align: center; margin: 20px 0;">
                <span style="font-size: 36px; font-weight: bold; color: #2E7D32; letter-spacing: 8px;">${resetToken}</span>
              </div>
              <p style="color: #757575; font-size: 14px; line-height: 1.6; margin: 20px 0;">
                ⏰ Mã này sẽ hết hạn sau <strong>1 giờ</strong>.
              </p>
              <p style="color: #757575; font-size: 14px; line-height: 1.6; margin: 20px 0;">
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
              </p>
              <hr style="border: none; border-top: 1px solid #E0E0E0; margin: 30px 0;">
              <p style="color: #9E9E9E; font-size: 12px; line-height: 1.6; margin: 0;">
                💡 Không chia sẻ mã này với bất kỳ ai.
              </p>
            </td>
          </tr>
          <tr>
            <td style="background-color: #F5F5F5; padding: 20px 30px; text-align: center;">
              <p style="color: #9E9E9E; font-size: 12px; margin: 0;">© 2024 EcoTrack 🌍</p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
    `;
  }

  async verifyConnection() {
    return { success: this.isConfigured, message: this.isConfigured ? 'Brevo API ready' : 'Not configured' };
  }
}

const emailService = new EmailService();
module.exports = emailService;
