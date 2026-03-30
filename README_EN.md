# FaceApiDemoV2 (Bản mô tả tiếng Việt)

Đây là dự án demo SDK Face-RK3399 V2.0, tập trung vào 2 nhóm chức năng:

## 1) FireflyApi
Mô-đun API phần cứng, phục vụ tích hợp nhanh các thiết bị ngoại vi trên bo mạch Firefly:
- IDCard / ICCard / NFC
- QRCode
- Điều khiển đèn bù sáng (hồng ngoại, trắng, đỏ, xanh lá)
- RS485/RS232
- Wiegand input/output
- Relay, tín hiệu mức, radar, nhiệt độ

## 2) FaceApi
Mô-đun nhận diện khuôn mặt:
- Khởi tạo SDK và nạp model/license
- Thu nhận khung hình camera
- Phát hiện và theo dõi khuôn mặt thời gian thực
- Trích xuất đặc trưng, tìm kiếm/so khớp
- Nhận diện thuộc tính khuôn mặt và quản lý dữ liệu người dùng

## Gợi ý tích hợp theo tài liệu Firefly
Tham khảo mục **SDK Access** trong tài liệu chính thức:
- https://wiki.t-firefly.com/en/Face-RK3399/face_sdk_v2_0.html#sdk-access

Luồng tích hợp đề xuất:
1. Khởi tạo dịch vụ/SDK ở `onCreate()` hoặc `onResume()` tùy API.
2. Kiểm tra khả năng hỗ trợ phần cứng trước khi bật chức năng.
3. Đăng ký callback dữ liệu thời gian thực (ID card, QR, serial, radar, khuôn mặt...).
4. Giải phóng tài nguyên và hủy bind service ở `onStop()`/`onDestroy()`.

## Tải bản demo
- [Download APK](doc/FaceApiDemoV2.apk)
- [Tài liệu chi tiết](doc/English)
