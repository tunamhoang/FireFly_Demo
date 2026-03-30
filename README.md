# FaceApiDemoV2
Dự án gồm 2 mô-đun chức năng chính: **FireflyApi** và **FaceApi**.

## FireflyApi
`FireflyApi` cung cấp các khả năng điều khiển phần cứng và ngoại vi thường dùng trên thiết bị Face-RK3399, ví dụ:
- Đọc **CMND/CCCD** (IDCard)
- Đọc **thẻ IC / NFC**
- Quét **mã QR**
- Điều khiển đèn bù sáng, relay, tín hiệu mức, Wiegand, RS485/RS232, radar, nhiệt độ...

Mô-đun này phù hợp khi bạn muốn tích hợp nhanh các API phần cứng của Firefly vào nghiệp vụ ứng dụng.

## FaceApi
`FaceApi` là mô-đun nhận diện khuôn mặt, bao gồm:
- Phát hiện khuôn mặt
- Trích xuất đặc trưng và so khớp
- Nhận diện danh tính
- Phân tích thuộc tính (chất lượng, khẩu trang, giới tính, tuổi, sống/giả...)
- Quản lý dữ liệu khuôn mặt

## Lịch sử phiên bản
| Ngày | Phiên bản | Mô tả | Người cập nhật |
| ---- | ---- | ---- | ---- |
| 2021-06-01 | 1.3 | Khắc phục các vấn đề do khách hàng phản hồi | Xiao Datao · Song Jianfeng |
| 2020-12-21 | 1.2 | Tối ưu hiệu năng SDK, khắc phục các vấn đề do khách hàng phản hồi | Xiao Datao · Song Jianfeng |
| 2020-07-30 | 1.1 | Phiên bản V2 | Xiao Datao · Song Jianfeng |
| 2020-05-21 | 1.0 | Hoàn thành bản đầu tiên | Song Jianfeng |

## Môi trường phát triển
1. Android Studio >= 3.6.2
2. Gradle Version: 3.6.2
3. Android Gradle Plugin Version: 3.6.2
4. SDK Tools >= 25.2.3

## Tài nguyên
- [Tải APK](doc/FaceApiDemoV2.apk)
- [Tài liệu tiếng Việt](README_EN.md)
- [Gói thư viện arm64-v8a](doc/arm64-v8a)
- [Thư mục tài liệu chi tiết](doc)
