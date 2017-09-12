(function(AJS,$){
    $(function(){
        $('span.com-mesilat-check-cert-not-after').each(function(){
            var $span = $(this);
            $.ajax({
                url: AJS.contextPath() + '/rest/check-cert/1.0/cert/notAfter',
                type: 'GET',
                data: {
                    host: $span.attr('cert-data-host'),
                    port: $span.attr('cert-data-port')
                },
                dataType: 'json',
                context: $span
            }).done(function (data) {
                $(this).text(data.expires);
                if (data.days > 7){
                    $(this).addClass('com-mesilat-cert-ok');
                } else if (data.days > 3) {
                    $(this).addClass('com-mesilat-cert-warn');
                } else {
                    $(this).addClass('com-mesilat-cert-error');
                }
            }).fail(function (jqXHR) {
                $(this).text(jqXHR.responseText);
            });
        });
    });
})(AJS,AJS.$||$);