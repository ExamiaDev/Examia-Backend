$xml = [xml](Get-Content 'build\reports\jacoco\test\jacocoTestReport.xml')

"`nPER-CLASS METHOD missed (and which methods):"
foreach ($pkg in $xml.report.package) {
    foreach ($cls in $pkg.class) {
        $methodCounter = $cls.counter | Where-Object { $_.type -eq 'METHOD' }
        if ($null -ne $methodCounter -and [int]$methodCounter.missed -gt 0) {
            "`nCLASS: $($cls.name) - missed methods: $($methodCounter.missed)"
            foreach ($m in $cls.method) {
                $mInstr = $m.counter | Where-Object { $_.type -eq 'INSTRUCTION' }
                $mMissed = if ($null -ne $mInstr) { [int]$mInstr.missed } else { 0 }
                $mCovered = if ($null -ne $mInstr) { [int]$mInstr.covered } else { 0 }
                if ($mMissed -gt 0 -and $mCovered -eq 0) {
                    "  - NEVER COVERED: $($m.name)$($m.desc)"
                } elseif ($mMissed -gt 0) {
                    "  - PARTIAL ($mCovered covered, $mMissed missed): $($m.name)$($m.desc)"
                }
            }
        }
    }
}

"`nPER-CLASS BRANCH missed (top offenders):"
$branchRows = foreach ($pkg in $xml.report.package) {
    foreach ($cls in $pkg.class) {
        $b = $cls.counter | Where-Object { $_.type -eq 'BRANCH' }
        if ($null -ne $b -and [int]$b.missed -gt 0) {
            [PSCustomObject]@{
                Class  = $cls.name
                Missed = [int]$b.missed
                Covered = [int]$b.covered
            }
        }
    }
}
$branchRows | Sort-Object -Property Missed -Descending | Format-Table -AutoSize
