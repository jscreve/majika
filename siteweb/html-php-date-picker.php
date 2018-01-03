<select name="year">
  <option value="">Année</option>
  <?php 
	for ($year = date('Y'); $year > 2016; $year--) { ?>
	<option value="<?php echo $year; ?>" <?php if(isset($_POST['year']) && $_POST['year'] == $year) echo 'selected=\"selected\"'?> > <?php echo $year; ?>
    </option>
	<?php } ?>
</select>
<select name="month">
	<option value="">Mois</option>
	<?php for ($month = 1; $month <= 12; $month++) { ?>
	<option value="<?php echo strlen($month)==1 ? '0'.$month : $month; ?>" <?php if(isset($_POST['month']) && $_POST['month'] == $month) echo 'selected=\"selected\"'?> > <?php echo strlen($month)==1 ? '0'.$month : $month; ?></option>
	<?php } ?>
</select>
<select name="day">
  <option value="">Jour</option>
	<?php for ($day = 1; $day <= 31; $day++) { ?>
	<option value="<?php echo strlen($day)==1 ? '0'.$day : $day; ?>" <?php if(isset($_POST['day']) && $_POST['day'] == $day) echo 'selected=\"selected\"'?> ><?php echo strlen($day)==1 ? '0'.$day : $day; ?></option>
	<?php } ?>
</select>
