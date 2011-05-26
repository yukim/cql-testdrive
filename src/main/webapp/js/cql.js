(function() {
  CodeMirror.fromTextArea(document.getElementById("query"), {
    lineNumbers: false,
    matchBrackets: true,
    indentUnit: 4,
    mode: 'text/x-plsql'
  });

  $('#exec').submit(function(e){
    e.preventDefault();

    $('#btn').attr('disabled', 'disabled');
    $('#result').empty();

    $.ajax({
      url: '/cql',
      type: 'post',
      dataType: 'json',
      data: {'query': $('#query').val()},
      success: function(data) {
        console.log(data);
        var res = $('#result');
        if (data.rows.length > 0) {
          var ul = $('<ul/>');
          data.rows.forEach(function(row) {
            var li = $('<li>');
            Object.keys(row).forEach(function(key) {
              var cls = "col";
              if (key === "KEY") {
                cls = "primarykey";
              }
              li.append($(
                '<span class="' + cls + '">' + key + '</span><span class="val">' + row[key] + '</span>'
              ));
            });
            ul.append(li);
          });
          res.append(ul);
        } else {
          res.val("no result found.");
        }

        $('#btn').removeAttr('disabled');
      },
      error: function(xhr) {
        alert("Error: " + JSON.parse(xhr.responseText).reason);
        $('#btn').removeAttr('disabled');
      }
    });
  });
})();
