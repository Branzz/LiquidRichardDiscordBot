<?php

// Disallow direct access to this file for security reasons
if(!defined("IN_MYBB"))
{
	die("Direct initialization of this file is not allowed.");
}

$plugins->add_hook();

function lrb_info()
{
	return array(
		"name"			=> "Liquid Richard Discord Bot Tele Post",
		"description"	=> "WGTOW",
// 		"website"		=> "",
		"author"		=> "bran",
// 		"authorsite"	=> "",
		"version"		=> "1.0",
		"guid" 			=> "lrb1.0",
		"codename"		=> "lrb",
		"compatibility" => "*"
	);
}

function lrb_install()
{

}

function lrb_is_installed()
{
    return true;
}

function lrb_uninstall()
{

}

function lrb_activate()
{

}

function lrb_deactivate()
{

}

function lrb_cloak()
{
	global $mybb, $db, $thread, $uid, $username;

    // use the puppet uid instead of their real uid
    $uid = (int) $mybb->input['which_puppet'];
    $mybb->user = get_user($uid);
    $username = $mybb->user['username'];
    return;

	// update user stats:

	//// update their online status
	// $query = $db->simple_select('sessions', 'sid', "uid='{$uid}'");

	// if the user has a session then fetch it
// 	if ($db->num_rows($query) == 1) {
// 		$sid = $db->fetch_field($query, 'sid');
// 	}
//
// 	// if not
// 	if (!$sid) {
// 		// create it
// 		$mybb->session->create_session($uid);
// 		$sid = $mybb->session->sid;
// 	}
//
// 	if ($sid) {
// 		// update the session with fake data
// 		$fake_session = array (
// 			'sid' => $sid,
// 			'uid' => $uid,
// 			'time' => TIME_NOW,
// 			'ip' => PM_FAKE_IP,
// 			'location' => $fake_location
// 		);
// 		$db->update_query('sessions', $fake_session, "uid='{$uid}'");
// 	}

}
