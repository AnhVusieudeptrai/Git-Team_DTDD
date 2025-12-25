/**
 * Email Service - Gửi email cho các chức năng như quên mật khẩu
 * Sử dụng Resend API (miễn phí 3000 emails/tháng)
 */
class EmailService {
  constructor() {
    this.resend = null;
    this.isConfigured = false;
    this.fromEmail = 'EcoTrack <onboarding@resend.dev>'; // Default Resend domain
    this.init();
  }

  init() {
    if (!process.env.RESEND_API_KEY) {
      console.log('⚠️  Email service not configured. Set RESEND_API_KEY in .env');
      console.log('   Get free API key at: https://resend.com');
      return;
    }

    try {
      const { Resend } = require('resend');
      this.resend = new Resend(process.env.RESEND_API_KEY);
      this.isConfigured = true;
      
      // Nếu có custom domain
      if (process.env.RESEND_FROM_EMAIL) {
        this.fromEmail = process.env.RESEND_FROM_EMAIL;
      }
      
      console.log('✅ Email service configured (Resend)');
    } catch (error) {
      console.error('❌ Email service configuration failed:', error.message);
    }
  }

  /**
   * Gửi email đặt lại mật khẩu
   */
  async sendPasswordResetEmail(to, resetToken, username) {
    if (!this.isConfigured) {
      console.log('Email service not configured, skipping email send');
      return { success: false, message: 'Email service not configured' };
    }

    try {
      const { data, error } = await this.resend.emails.send({
        from: this.fromEmail,
        to: [to],
        subject: '🌿 EcoTrack - Mã đặt lại mật khẩu',
        html: this.getPasswordResetTemplate(username, resetToken)
      });

      if (error) {
        console.error('Resend error:', error);
        return { success: false, message: error.message };
      }

      console.log('Password reset email sent:', data.id);
      return { success: true, messageId: data.id };
    } catch (error) {
      console.error('Failed to send password reset email:', error);
      return { success: false, message: error.message };
    }
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
          <!-- Header -->
          <tr>
            <td style="background: linear-gradient(135deg, #2E7D32 0%, #66BB6A 100%); padding: 40px 20px; text-align: center;">
              <h1 style="color: #ffffff; margin: 0; font-size: 28px;">🌿 EcoTrack</h1>
              <p style="color: #C8E6C9; margin: 10px 0 0 0; font-size: 14px;">Sống xanh mỗi ngày</p>
            </td>
          </tr>
          
          <!-- Content -->
          <tr>
            <td style="padding: 40px 30px;">
              <h2 style="color: #1B5E20; margin: 0 0 20px 0; font-size: 24px;">Xin chào ${username || 'bạn'}! 👋</h2>
              
              <p style="color: #424242; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản EcoTrack của bạn.
              </p>
              
              <p style="color: #424242; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                Mã xác nhận của bạn là:
              </p>
              
              <!-- Reset Code Box -->
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
          
          <!-- Footer -->
          <tr>
            <td style="background-color: #F5F5F5; padding: 20px 30px; text-align: center;">
              <p style="color: #9E9E9E; font-size: 12px; margin: 0;">
                © 2024 EcoTrack 🌍
              </p>
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
    return { success: this.isConfigured, message: this.isConfigured ? 'Ready' : 'Not configured' };
  }
}

const emailService = new EmailService();
module.exports = emailService;
