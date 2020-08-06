// =====================================================
// Fix for issue with accessible-autocomplete which prevents submission of empty values
// https://github.com/alphagov/accessible-autocomplete/issues/432
//
// Adapted from this fix in the amls-frontend project
// https://github.com/hmrc/amls-frontend/pull/1457/commits/974839e999c06083abafb1a402c3ea30fd4b57c6
// =====================================================


$(document).ready(function(){

    $('[data-gov-select-autocomplete]').each(function() {

        var selectFieldName = $(this).attr('id').replace('[', '\\[').replace(']', '\\]');
        var nonSelectFieldName = selectFieldName.replace('-select','');

        var selectField = $('#' + selectFieldName)
        var nonSelectField = $('#' + nonSelectFieldName)

        nonSelectField.keydown(function(e) {
            if (e.keyCode === 13 && $(this).val() === '') {
                selectField.val('')
            }
        }).keyup(function() {
            var menu = $('.autocomplete__menu')
            if (menu.text() === 'No results found') {
                selectField.val('')
            }
        }).attr('name', nonSelectFieldName + '-autocomp');

        $('body')
            .on('mouseup', ".autocomplete__option > strong", function(e){
                e.preventDefault();
                $(this).parent().trigger('click')
            }).on('click', '.autocomplete__option', function(evt) {
            evt.preventDefault()
            var e = jQuery.Event('keydown');
            e.keyCode = 13;
            $(this).closest('.autocomplete__wrapper').trigger(e);
        })

        $('button.govuk-button').click(function(){

            var selectedOption = $('#' + selectFieldName + ' option:selected')

            if(nonSelectField.val() === '')
                selectField.val('');

            if (selectField.val() === "" && nonSelectField.val() !== "" || selectedOption.text() !== nonSelectField.val())
                addOption(nonSelectField.val())

            function addOption(value) {
                $("<option data-added='true'>")
                    .attr("value", value)
                    .text(value)
                    .prop("selected", true)
                    .appendTo($('#' + selectFieldName))
            }
        })
    })

});