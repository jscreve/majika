<select name="year_month">
  <option value="">Année</option>
  <?php 
	for ($year = date('Y'); $year > 2015; $year--) { ?>
	<option value="<?php echo $year; ?>" <?php if(isset($_POST['year_month']) && $_POST['year_month'] == $year) echo 'selected=\"selected\"'?> > <?php echo $year; ?>
    </option>
	<?php } ?>
</select>
<select name="month_month">
	<option value="">Mois</option>
	<?php for ($month = 1; $month <= 12; $month++) { ?>
	<option value="<?php echo strlen($month)==1 ? '0'.$month : $month; ?>" <?php if(isset($_POST['month_month']) && $_POST['month_month'] == $month) echo 'selected=\"selected\"'?> > <?php echo strlen($month)==1 ? '0'.$month : $month; ?></option>
	<?php } ?>
</select>
