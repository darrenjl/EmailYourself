// This callback function is called when the content script has been 
// injected and returned its results
function onPageInfo(o)  { 
    sendEmail(decodeURIComponent(localStorage.email), o.title, encodeURIComponent(o.url));    
} 

// Global reference to the status display SPAN
var statusDisplay = null;

function emailExists(){
    email = localStorage.email;

    if(email == undefined || email == "") {
        return false;
    }
    return true;    
};

function saveEmail() {
    event.preventDefault();

    var email = encodeURIComponent(document.getElementById('email').value);    
    console.log(email);
    localStorage.email = email;
    chrome.extension.getBackgroundPage().getPageInfo(onPageInfo); 
};

function sendEmail(email, subject, body) {
    $.ajax({ 
             type: "GET",
             dataType: "json",
             url: "https://script.google.com/macros/s/AKfycbwpNFmh9INcnlaLdW5WZxKVaWDaFMljAI6Mo4EJ_qYdW6XbRC2F/exec?email=" + email + "&subject=" + subject + "&body=" + body,
             success: function(data){        
                alert(data);
                window.close();
             },
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus);
                alert(errorThrown);
            }
         });
};

// When the popup HTML has loaded
window.addEventListener('load', function(evt) {
    // Handle the bookmark form submit event with our addBookmark function
    document.getElementById('addemail').addEventListener('submit', saveEmail);
    // Cache a reference to the status display SPAN
    statusDisplay = document.getElementById('status-display');
    // Call the getPageInfo function in the background page, injecting content_script.js 
    // into the current HTML page and passing in our onPageInfo function as the callback
    exists = emailExists();
    if (exists) {        
        $("#emailDiv").hide();
        $("#sendingDiv").show();
        chrome.extension.getBackgroundPage().getPageInfo(onPageInfo);        
    } else {
        console.error('no email provided');
    }
});