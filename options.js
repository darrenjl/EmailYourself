function updateEmail() {
    event.preventDefault();

    var email = encodeURIComponent(document.getElementById('email').value);    
    localStorage.email = email;
    window.close();
}

window.addEventListener('load', function(evt) {
    document.getElementById('updateemail').addEventListener('submit', updateEmail);
    document.getElementById('email').value = localStorage.email;
});