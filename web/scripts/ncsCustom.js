/** react on the changes in the text fields and call Ajax
 */
function registerChange( form, field )
{
	try
	{
		var fldValue = field.getValue();
		var varURL = form.getValue( "fldEndPoint" );

        if ( fldValue == "" || varURL == "") return;
		ajax_call_jquery( form, varURL );
	}
	catch( e )
	{
		form.showError( e );
	}
}

/** collect the data that we want to send to the service, then call Ajax
 */
function ajax_call_jquery( form, url )
{
	try
	{
		var inputData	= new Object();
                
                inputData[ "user" ] = form.getValue( "recipient" );
                inputData[ "pwd" ] = form.getValue( "password" );

		// call to jQuery Ajax
		$.ajax( {
			type:		'GET',
			url:		url,
			data:		inputData,
			success: function( data )
			{
				ajax_display( form, url, data );
			},
			error:	function( xhr, status )
			{
				ajax_display( form, url, xhr + " / " + status );
			},
			timeout: 9000
		} )
	}
	catch( e )
	{
		form.showError( e );
	}
}

/** this event handler is called on return from the Ajax call
 *
 *		evaluate/display server response
 */
function ajax_display( form, url, data )
{
	if ( data )
	{
		form.setValues( "fldResult", dojo.toJson( data ) );
		if ( data.time ) alert( "Server date/time: " + data.time );
	}
}