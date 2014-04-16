document.addEventListener('DOMContentLoaded', function () {
  alert("test");
  chrome.tabs.getCurrent(function(tab){
      alert(tab.url);
  });
});