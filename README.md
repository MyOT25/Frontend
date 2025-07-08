# 🎟️ MyOT Android
MyOT 안드로이드 파트 리포지토리입니다.

---

## 👥 팀원 소개

| 김가윤 | 김시환 | 황승민 |
|:---:|:---:|:---:|
| <img src="https://github.com/JCTA0125.png" width="240" height="180"> | <img src="https://github.com/SihwanGit.png" width="240" height="180"> | <img src="https://github.com/sxunxin.png" width="240" height="180"> |
| [@JCTA0125](https://github.com/JCTA0125) | [@SihwanGit](https://github.com/SihwanGit) | [@sxunxin](https://github.com/sxunxin) |

---

## ⚙️ Tech Stack

| 기술 | 설명 |
|------|------|
| Kotlin | 메인 프로그래밍 언어 |
| XML Layout | XML 기반 UI 설계 |
| Git | 체계적인 코드 관리 및 협업 |

### 📦 사용 라이브러리

| 라이브러리 | 버전 | 설명 |
|------------|------|------|
| Glide | 4.16.0 | 이미지 로딩 및 캐싱 |
| Parcelize | Kotlin 내장 | 객체 직렬화를 위한 코틀린 플러그인 |
| SharedPreference | 1.2.1 | 로컬 데이터 저장 |
| Room | 2.6.1 | 로컬 데이터베이스 |
| CoordinatorLayout | 1.3.0 | 화면 구성 및 동작 제어 |
| Fragment KTX | 1.8.8 | 프래그먼트 편의 기능 제공 |

> 본 프로젝트는 **멀티 모듈 아키텍처** 및 **MVVM 패턴**을 기반으로 구성됩니다.

---

## 🧭 Git Conventions

### 📌 Branch 전략

- 메인 브랜치: `main`
- 기능 개발 시 화면 또는 역할 기준으로 브랜치 명명 (영역/기능 형태)
  - 예시: `community/review`, `home/feed`

**작업 흐름**  
1. 기능 이슈 생성 → 번호 발급  
2. `main` → `기능 브랜치` 생성 후 작업  
3. 작업 완료 → `main` 브랜치로 Pull Request 생성

### 📌 작업 템플릿 가이드

작업 유형에 따라 명확하게 커밋 타입을 구분합니다.

| 타입 | 용도 |
|------|------|
| **Feat** | 새로운 기능 추가 |
| **Fix** | 버그 수정 |
| **Refactor** | 코드 리팩토링 (동작 변화 없이 구조 개선) |
| **Docs** | 문서 작성 및 수정 (README, 주석 등) |
| **Style** | 코드 포맷, 네이밍, 세미콜론 등 스타일 변경 (기능 무관) |
| **Test** | 테스트 코드 추가 및 수정 |
| **Chore** | 설정, 빌드, 패키지 등 기타 변경 작업 |

---

### ✅ Commit 템플릿

```text
[타입] 간단한 설명

- 작업한 내용에 대한 구체적인 설명
- 필요한 경우 여러 줄로 상세하게 작성
```

### 📝 Pull Request 템플릿

```txt
[타입] 간단한 설명

## 작업 내용
- 무엇을 변경했는지 간단히 작성

## 참고 사항
- 리뷰 시 유의해야 할 사항
```


### 💡 Issue 템플릿

```txt
[타입] 이슈 제목

## 이슈 개요
- 어떤 작업인지 간략히 설명해주세요.

## 이슈 유형
- [ ] Feat: 새로운 기능 추가
- [ ] Fix: 버그 수정
- [ ] Refactor: 리팩토링 (기능 변화 없음)
- [ ] Docs: 문서 작성 또는 수정
- [ ] Chore: 설정, 환경 구성 등

## 작업 항목
- [ ] 작업 1
- [ ] 작업 2
- [ ] 작업 3

## 참고 자료
- 관련 문서, 디자인, 링크 등
```

---

## 🧑‍💻 Code Style

본 프로젝트는 [Google Kotlin 스타일 가이드](https://developer.android.com/kotlin/style-guide?hl=ko)를 따르며, 일관된 네이밍 규칙을 유지합니다.

### 🔤 네이밍 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| **Class** | PascalCase | `MainActivity`, `FeedViewModel` |
| **Function / Variable** | camelCase | `getFeedList()`, `currentPosition` |
| **Constant** | UPPER_SNAKE_CASE | `MAX_FEED`, `DEFAULT_TIME` |
| **Layout XML** | lowercase_snake_case | `activity_main.xml`, `item_feed.xml` |
| **Resource ID** | lowercase_snake_case | `btn_like`, `tv_title` |

### 📌 기타 규칙

- 콜백 함수는 `on` 접두어 사용 → 예: `onItemClick`, `onLoaded`
- 모든 소스 파일은 UTF-8 인코딩을 사용해야 합니다.
  
---

## 🧪 개발 환경

| 항목 | 정보 |
|------|------|
| min SDK | 28 |
| target SDK | 35 |
| IDE | Android Studio Meerkat (2024.3.1) |
| 테스트 기기 | Emulator : Medium Phone API 36 |

---
