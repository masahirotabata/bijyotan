// ✅ checkUserState関数（仮実装）
function checkUserState(userId) {
  console.log("checkUserState is not implemented yet. userId:", userId);
  // 必要ならここでプレミアム状態や広告視聴状況を確認し、UI制御も可
}

// ✅ isAdUnlocked関数（仮実装）
function isAdUnlocked() {
  // localStorageにフラグがあるか確認（5分以内かなど）
  const unlockTime = localStorage.getItem("adUnlockedUntil");
  if (!unlockTime) return false;

  const now = new Date().getTime();
  return now < parseInt(unlockTime);
}

function getUserIdFromQuery() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('userId');
}
document.addEventListener('DOMContentLoaded', function () {
  const userId = getUserIdFromQuery(); // クエリパラメータから取得
  let currentPart = "Part1"; // デフォルト

  fetchWords(userId, currentPart);
  checkUserState(userId);

  // Partボタンを切り替えた時の処理
  document.querySelectorAll("button[id^='partButton']").forEach(button => {
    button.addEventListener("click", () => {
      currentPart = button.dataset.part;
      fetchWords(userId, currentPart);
    });
  });

document.getElementById("importCsvButton").addEventListener("click", function () {
  const fileInput = document.getElementById("fileInput");
  const userId = document.getElementById("userIdHidden").value;

  if (!fileInput.files.length) {
    alert("CSVファイルを選択してください");
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);

  fetch("/api/words/import", {
    method: "POST",
    body: formData
  })
    .then(response => response.text())
    .then(result => {
      alert(result);
      location.reload(); // 必要に応じて
    })
    .catch(error => {
      alert("アップロード失敗: " + error);
    });
});


// 👇 userIdとpartを引数で受け取るように修正
async function fetchWords(userId, part) {
  const res = await fetch(`/api/words?userId=${userId}&part=${part}`);
  const words = await res.json();
  const ul = document.getElementById('wordList');
  ul.innerHTML = '';

  const userRes = await fetch(`/user/${userId}`);
  const user = await userRes.json();
  const adUnlocked = isAdUnlocked();
  const showBeautyButton = user.isPremium || adUnlocked;

  words.forEach(word => {
    const staticImgSrc = getStaticImageForWord(word.word);
    const motionVideoSrc = getMotionVideoForWord(word.word);

    const li = document.createElement('li');
    li.innerHTML = `
      <div style="background: #ccff33; padding: 10px; margin-bottom: 10px;">
        <strong>${word.word}</strong>（${word.meaning}）<br>
        描写: ${word.pictDescription || 'なし'}<br>
        ステータス: ${word.status ? 'OK済' : '未学習'}
        <div style="margin-top: 10px;">
          <img id="staticImage-${word.id}" src="${staticImgSrc}" width="240" alt="美女" />
          <video id="motionVideo-${word.id}" width="240" style="display:none;" muted loop>
            <source src="${motionVideoSrc}" type="video/mp4" />
            お使いのブラウザは動画に対応していません。
          </video><br>
          <button onclick="markOk(${word.id})" style="background: red; color: white;">OK</button>
          <button onclick="deleteWord(${word.id})" style="background: blue; color: white;">削除</button>
          ${
            showBeautyButton
              ? `<button onclick="playMotion(${word.id})" style="margin-left: 10px;">美女を動かす</button>`
              : ''
          }
        </div>
      </div>
    `;
    ul.appendChild(li);
  });

  const adBtn = document.getElementById('watchAdButton');
  adBtn.style.display = (!user.isPremium && !adUnlocked) ? 'inline-block' : 'none';
}
});
