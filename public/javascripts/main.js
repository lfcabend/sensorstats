  $( function() {
    $( "#from" ).datepicker();
    $( "#to" ).datepicker();


    $( "#today" ).click(function() {
        var d = new Date();
        var todayStr = createStringFromDay(d)
        $( "#from" ).val(todayStr);
        $( "#to" ).val(todayStr);
      });

    $( "#week" ).click(function() {
        var d = new Date();
        var monday = getMonday(d);
        var mondayStr = createStringFromDay(monday);
        var todayStr = createStringFromDay(d);
        $( "#from" ).val(mondayStr);
        $( "#to" ).val(todayStr);
      });

    $( "#month" ).click(function() {
        var d = new Date();
        var firstDay = new Date(d.getFullYear(), d.getMonth(), 1);
        var firstDayStr = createStringFromDay(firstDay);
        var todayStr = createStringFromDay(d);
        $( "#from" ).val(firstDayStr);
        $( "#to" ).val(todayStr);
    });

    function createStringFromDay(d)  {
        return ((d.getMonth() + 1) + "").padStart(2, "0")  + "/" +
         (d.getDate() + "").padStart(2, "0") + "/" + d.getFullYear()
    }

    function getMonday(d) {
      d = new Date(d);
      var day = d.getDay(),
          diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
      return new Date(d.setDate(diff));
    }

  } );