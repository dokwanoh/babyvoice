# Galaxy 음성 호출 설정 안내

Galaxy 기기에서 음성으로 앱을 호출하는 기준 경로입니다.

이 앱은 일반 앱 실행 Intent로 열리면 즉시 전체 브리핑을 읽습니다. 따라서 Google/Gemini 또는 Bixby가 앱을 열 수 있으면 별도 화면 탭 없이 음성 브리핑까지 이어집니다.

Galaxy Tab A11+ 실기기에서 앱 전환이 확인된 문구:

```text
오케이 구글, 아기 상태 열어줘
```

앱은 다음 런처 이름을 제공합니다.

- `아기 브리핑`
- `아기 상태`
- `베이비 브리핑`
- `Baby Voice`
- `아기 기록`
- `육아 기록`
- `베이비 타임` (`demo` 빌드의 개인 테스트용 별칭)

Google/Gemini에서 먼저 시도할 문구:

1. `오케이 구글, 베이비 타임`
2. `오케이 구글, 베이비 타임 열어줘`
3. `오케이 구글, 아기 상태 열어줘`
4. `오케이 구글, 아기 기록 열어줘`
5. `오케이 구글, 육아 기록 열어줘`
6. `오케이 구글, 아기 브리핑 열어줘`
7. `오케이 구글, 베이비 브리핑 열어줘`

Bixby Quick Command를 쓰는 경우:

1. 아기 브리핑 앱을 설치하고 한 번 실행합니다.
2. Bixby 또는 모드 및 루틴의 Quick Command 설정 화면을 엽니다.
3. 새 명령 문구를 `아기 브리핑`, `아기 상태`, `아기 기록`, `육아 기록` 중 하나로 입력합니다.
4. 실행 동작으로 `앱 열기`를 선택합니다.
5. 앱 목록에서 `아기 상태`, `아기 기록`, `육아 기록`, `아기 브리핑` 중 하나를 선택합니다.
6. 저장한 뒤 실제로 `하이 빅스비, 아기 상태` 또는 `하이 빅스비, 아기 기록`이라고 말합니다.
7. 앱이 열리고 바로 브리핑 음성이 나오는지 확인합니다.

앱 열기 동작을 선택할 수 없는 기기에서는 보조 경로를 사용합니다.

1. 빠른 설정 패널을 열고 `아기 브리핑` 타일을 추가합니다.
2. 타일을 눌러 앱이 브리핑 화면으로 열리는지 확인합니다.
3. Quick Command 실행 동작으로 빠른 설정 타일 실행을 선택할 수 있으면 `아기 브리핑` 타일을 연결합니다.
4. 딥 링크를 직접 연결할 수 있는 경우 `babyvoice://briefing`를 연결 대상으로 선택합니다.

ADB로 Bixby의 앱 열기와 같은 런처 경로를 확인할 때:

```bash
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.MainActivity
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyStatusLauncherAlias
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyLogLauncherAlias
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.CareLogLauncherAlias
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyTimeLauncherAlias
```

오디오 출력까지 확인할 때는 앱 화면의 상태가 `말하는 중`으로 바뀌고, `dumpsys audio`에 `callingPack=com.babyvoice.bridge.demo.debug`의 `requestAudioFocus()`가 표시되어야 합니다.

ADB로 빠른 설정 타일 경로를 확인할 때:

```bash
adb shell cmd statusbar add-tile com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.integration.assistant.BriefingTileService
adb shell cmd statusbar click-tile com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.integration.assistant.BriefingTileService
```

주의:

- 브랜드명은 BabyTime가 아니라 `아기 브리핑`을 사용합니다.
- `베이비 타임` 별칭은 현재 개인 테스트용 `demo` 빌드에만 포함합니다. production 배포에는 별도 서면 승인이 필요합니다.
- `오케이 구글, 베이비 타임`처럼 동사 없는 호출은 앱이 직접 강제할 수 없고 Google/Gemini가 해당 문구를 앱/shortcut으로 라우팅해야 합니다. demo 빌드에는 launcher alias, static shortcut, query pattern을 모두 추가해 후보를 늘렸습니다.
- 이 앱은 기본 비서 교체 기능을 제공하지 않습니다.
- 사용자가 직접 등록한 명령만 동작해야 합니다.
- Google/Gemini의 App Actions 직접 호출이 아니라 일반 앱 열기 경로로 검증했습니다.
- 2026-07-17 실기기 재검증에서 앱 전환만으로는 충분하지 않았고, TTS 오디오 포커스 요청까지 확인해야 함을 확인했습니다.
- 실제 BabyTime 연동은 공식 API, 사용자 선택 내보내기, 사용자 허용 알림 정보 같은 승인된 경로가 필요합니다.
