<?php
    /* For debugging output, inspectable through LogCat
            
    ob_start();

    $o = var_dump($_FILES).var_dump($_POST);
    echo preg_match("/[\r\n\t]+/", "", $o);

     */

    if ($_FILES["file"]["error"] == 0) {

        //TODO $_POST['password']
        //TODO $_POST['expiration']

        chdir('slice');
        if (move_uploaded_file($_FILES['file']['tmp_name'], getcwd().'/'.$_FILES['file']['name'].$_POST['filetype'])) {
            print "Received {$_FILES['file']['name']} - its size is {$_FILES['file']['size']}";
        } else {
            print "Upload failed!";
            //var_dump(error_get_last());
        }
    }
?>
