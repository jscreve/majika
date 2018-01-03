<select name="year_year">
  <option value="">Année</option>
  <?php 
	for ($year = date('Y'); $year > 2016; $year--) { ?>
	<option value="<?php echo $year; ?>" <?php if(isset($_POST['year_year']) && $_POST['year_year'] == $year) echo 'selected=\"selected\"'?> > <?php echo $year; ?>
    </option>
	<?php } ?>
</select>
